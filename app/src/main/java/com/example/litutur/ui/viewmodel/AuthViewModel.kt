package com.example.litutur.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.litutur.data.model.ChildProfileEntity
import com.example.litutur.data.model.ParentUser
import com.example.litutur.data.remote.CloudinaryUploader
import com.example.litutur.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val parentUser: ParentUser? = null,
    val children: List<ChildProfileEntity> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentSession()
    }

    fun checkCurrentSession() {
        val uid = repository.currentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isCheckingSession = false, isLoggedIn = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingSession = true) }
            val parentResult = repository.getParentProfile(uid)
            val childrenResult = repository.getChildren(uid)
            _uiState.update {
                it.copy(
                    isCheckingSession = false,
                    isLoggedIn = parentResult.isSuccess,
                    parentUser = parentResult.getOrNull(),
                    children = childrenResult.getOrDefault(emptyList())
                )
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (hasChildren: Boolean) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email dan kata sandi wajib diisi.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { parent ->
                    val children = repository.getChildren(parent.uid).getOrDefault(emptyList())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isGuestMode = false,
                            parentUser = parent,
                            children = children
                        )
                    }
                    onSuccess(children.isNotEmpty())
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal masuk ke akun."
                        )
                    }
                }
            )
        }
    }

    fun registerParent(
        name: String,
        email: String,
        whatsapp: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        when {
            name.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Nama lengkap orang tua wajib diisi.") }
                return
            }
            email.isBlank() || !email.contains("@") -> {
                _uiState.update { it.copy(errorMessage = "Masukkan alamat email yang valid.") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Kata sandi minimal 6 karakter.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = repository.registerParent(name, email, whatsapp, password)
            result.fold(
                onSuccess = { parent ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isGuestMode = false,
                            parentUser = parent,
                            children = emptyList()
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal mendaftarkan akun."
                        )
                    }
                }
            )
        }
    }

    fun saveChildProfile(
        name: String,
        ageRange: String,
        avatarName: String,
        favoriteTopics: List<String>,
        photoUri: Uri? = null,
        context: Context? = null,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama panggilan anak wajib diisi.") }
            return
        }

        val uid = repository.currentUserId()
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Sesi akun tidak ditemukan. Silakan masuk ulang.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

            var uploadedAvatarUrl = ""
            if (photoUri != null && context != null) {
                val uploadResult = CloudinaryUploader.uploadImage(
                    context = context,
                    imageUri = photoUri,
                    folder = "litutur/avatars"
                )
                uploadResult.fold(
                    onSuccess = { url -> uploadedAvatarUrl = url },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal mengunggah foto profil: ${err.message}"
                            )
                        }
                        return@launch
                    }
                )
            }

            val parsedAge = when (ageRange) {
                "3-4 Thn" -> 4
                "5-6 Thn" -> 6
                "7-8 Thn" -> 8
                else -> 9
            }

            val child = ChildProfileEntity(
                name = name.trim(),
                ageRange = ageRange,
                age = parsedAge,
                avatarName = avatarName,
                avatarUrl = uploadedAvatarUrl,
                favoriteTopics = favoriteTopics,
                statusLevel = "Pemula Ceria"
            )

            val result = repository.saveChildProfile(uid, child)
            result.fold(
                onSuccess = { savedChild ->
                    val updatedChildren = _uiState.value.children + savedChild
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            children = updatedChildren
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal menyimpan profil anak."
                        )
                    }
                }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Masukkan alamat email terlebih dahulu untuk reset kata sandi.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = repository.sendPasswordReset(email)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Link reset kata sandi telah dikirim ke $email."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal mengirim email reset."
                        )
                    }
                }
            )
        }
    }

    fun enterGuestMode() {
        _uiState.update {
            it.copy(
                isGuestMode = true,
                isLoggedIn = false,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update {
            AuthUiState(isCheckingSession = false)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}
