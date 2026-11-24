package com.example.litutur.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.LituturTheme
import com.example.litutur.ui.viewmodel.AutoMilestoneOption
import com.example.litutur.ui.viewmodel.ParentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    uiState: ParentUiState = ParentUiState(),
    onBack: () -> Unit = {},
    onPublishPost: (caption: String, milestone: AutoMilestoneOption?, bookTitle: String?, photoUri: Uri?) -> Unit = { _, _, _, _ -> }
) {
    var postText by remember { mutableStateOf("") }
    var bookTitle by remember { mutableStateOf("") }
    var selectedMilestoneId by remember {
        mutableStateOf(uiState.autoMilestones.firstOrNull()?.id ?: "none")
    }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedPhotoUri = uri
    }

    val selectedMilestone = uiState.autoMilestones.firstOrNull { it.id == selectedMilestoneId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Postingan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0D1C2E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !uiState.isSubmitting) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onPublishPost(postText, selectedMilestone, bookTitle, selectedPhotoUri)
                        },
                        enabled = !uiState.isSubmitting && postText.isNotBlank(),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralOrangePrimary),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(36.dp)
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(text = "Posting", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FF))
            )
        },
        containerColor = Color(0xFFF8F9FF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Author Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = CoralOrangePrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = uiState.parentUser?.name?.ifBlank { "Orang Tua Litutur" } ?: "Orang Tua Litutur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = uiState.selectedChild?.let { "Membagikan progres ${it.name} (${it.ageRange})" }
                                ?: "Komunitas Orang Tua Litutur",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(uiState.errorMessage, color = Color(0xFFDC2626), fontSize = 12.sp)
                }
            }

            // Caption Input
            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                placeholder = { Text("Bagikan cerita, tips membaca, atau perkembangan si kecil...") },
                minLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = CoralOrangePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Photo Upload via Cloudinary
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lampirkan Foto (Opsional)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        if (selectedPhotoUri != null) {
                            IconButton(onClick = { selectedPhotoUri = null }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus Foto", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Preview Foto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        enabled = !uiState.isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = CoralOrangePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedPhotoUri == null) "Pilih Foto dari Galeri" else "Ganti Foto",
                            color = CoralOrangePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Automatic Milestone Attachment Picker (Fungsi 4 Problem Solving)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lampirkan Milestone Anak (Otomatis dari Data)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Option: None
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMilestoneId = "none" }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMilestoneId == "none",
                            onClick = { selectedMilestoneId = "none" }
                        )
                        Text("Tidak ada lampiran milestone", fontSize = 13.sp, color = Color(0xFF475569))
                    }

                    // Auto-generated milestones from child's Firestore stats
                    uiState.autoMilestones.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedMilestoneId == option.id) Color(0xFFECFDF5) else Color.Transparent
                                )
                                .clickable { selectedMilestoneId = option.id }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMilestoneId == option.id,
                                onClick = { selectedMilestoneId = option.id }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${option.emoji} ${option.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${option.subtitle} • ${option.stats}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            // Optional Book Recommendation Tag
            OutlinedTextField(
                value = bookTitle,
                onValueChange = { bookTitle = it },
                label = { Text("Judul Cerita Terkait (Opsional)") },
                placeholder = { Text("mis. Singa yang Baik Hati") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = CoralOrangePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    LituturTheme {
        CreatePostScreen()
    }
}
