package com.example.litutur.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.data.model.CeritaBukuModel
import com.example.litutur.ui.components.ChildDetailTopBar
import com.example.litutur.ui.components.ChildIconAction
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun BacaCeritaScreen(
    bookTitle: String = "Singa yang Baik Hati",
    book: CeritaBukuModel? = null,
    chapterIndex: Int = 0,
    onBackClick: () -> Unit = {},
    onPreviousChapter: () -> Unit = {},
    onNextChapter: () -> Unit = {}
) {
    val context = LocalContext.current
    // Reset tooltip kosakata setiap kali pindah bab
    var showVocabTooltip by remember(chapterIndex) { mutableStateOf(false) }
    var showRoarEffect by remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var isTtsReady by remember { mutableStateOf(false) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("id", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.language = Locale.getDefault()
                }
                textToSpeech?.setPitch(1.15f) // Suara ramah & menyenangkan untuk anak
                textToSpeech?.setSpeechRate(0.85f) // Pelafalan jelas dan tenang
                isTtsReady = true
            }
        }
        ttsInstance = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    fun speak(text: String) {
        if (isTtsReady && ttsInstance != null) {
            ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LituturUtterance_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        ttsInstance?.stop()
    }

    val chapters = book?.chapters.orEmpty()
    val totalChapters = chapters.size
    val chapter = chapters.getOrNull(chapterIndex)
    val isLastChapter = chapterIndex >= totalChapters - 1
    val vocabWord = chapter?.vocabWord?.takeIf { it.isNotBlank() }
    val vocabMeaning = chapter?.vocabMeaning.orEmpty()

    // Narasi dibangun dari bab yang sedang dibuka (bukan teks contoh statis)
    val fullStoryNarration = remember(bookTitle, chapterIndex, chapter) {
        buildString {
            append("Bab ${chapterIndex + 1} dari ${totalChapters.coerceAtLeast(1)}. ")
            chapter?.title?.takeIf { it.isNotBlank() }?.let { append(it).append(". ") }
            append(chapter?.textContent.orEmpty())
        }
    }

    // Pindah bab: hentikan narasi bab sebelumnya, lalu bacakan bab baru jika narasi aktif
    LaunchedEffect(chapterIndex, isTtsReady) {
        stopSpeaking()
        if (isAudioPlaying && chapter != null) speak(fullStoryNarration)
    }

    LaunchedEffect(showRoarEffect) {
        if (showRoarEffect) {
            delay(1200)
            showRoarEffect = false
        }
    }

    // Buku belum selesai dimuat (atau judul belum cocok): tampilkan loading
    if (chapter == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(LituturBgStart, LituturBgMid, LituturBgEnd))),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = LituturPurple)
        }
        return
    }

    Scaffold(
        topBar = {
            ChildDetailTopBar(
                title = bookTitle,
                onBack = {
                    stopSpeaking()
                    onBackClick()
                }
            ) {
                // Tombol narasi (aktif / nonaktif)
                ChildIconAction(
                    icon = if (isAudioPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (isAudioPlaying) "Matikan Suara Narasi" else "Nyalakan Suara Narasi",
                    containerColor = if (isAudioPlaying) LituturTertiaryFixed else LituturSurfaceVariant,
                    tint = if (isAudioPlaying) LituturTertiary else LituturOutline,
                    onClick = {
                        isAudioPlaying = !isAudioPlaying
                        if (isAudioPlaying) {
                            speak(fullStoryNarration)
                            Toast.makeText(context, "🔊 Suara narasi aktif", Toast.LENGTH_SHORT).show()
                        } else {
                            stopSpeaking()
                            Toast.makeText(context, "🔇 Suara narasi dinonaktifkan", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(LituturBgStart, LituturBgMid, LituturBgEnd)))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LituturSurfaceHigh)
            ) {
                AsyncImage(
                    model = chapter.illustrationUrl.ifBlank { book?.coverUrl.orEmpty() },
                    contentDescription = chapter.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.35f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = LituturPrimary, modifier = Modifier.size(16.dp))
                        Text("Bab ${chapterIndex + 1} dari $totalChapters", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }

                    // Interactive Audio Status Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isAudioPlaying) LituturPrimaryContainer else Color.Black.copy(alpha = 0.55f))
                            .clickable {
                                isAudioPlaying = !isAudioPlaying
                                if (isAudioPlaying) {
                                    speak(fullStoryNarration)
                                    Toast.makeText(context, "🔊 Suara narasi aktif", Toast.LENGTH_SHORT).show()
                                } else {
                                    stopSpeaking()
                                    Toast.makeText(context, "🔇 Suara narasi dinonaktifkan", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAudioPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            if (isAudioPlaying) "Suara Aktif 🔊" else "Suara Mati 🔇",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showRoarEffect,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(LituturPrimary)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("Hore! ✨", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Interactive character sound button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable {
                            showRoarEffect = true
                            speak(
                                vocabWord?.let { "Kata baru hari ini: $it. $vocabMeaning" }
                                    ?: "Hore! Ayo lanjutkan membaca ${chapter.title.ifBlank { bookTitle }}!"
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = LituturPrimaryContainer, modifier = Modifier.size(16.dp))
                        Text("Ketuk gambar untuk kejutan! 🐾", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LANGKAH LITERASI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturOutline)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(totalChapters) { i ->
                                when {
                                    i < chapterIndex -> Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(LituturSecondaryDim),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }

                                    i == chapterIndex -> Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(LituturPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }

                                    else -> Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(LituturSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = chapter.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LituturPurpleDark
                    )

                    Text(
                        text = chapter.textContent,
                        fontSize = 17.sp,
                        lineHeight = 28.sp,
                        color = LituturTextDark
                    )

                    if (vocabWord != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LituturTertiaryFixed)
                                .clickable {
                                    showVocabTooltip = !showVocabTooltip
                                    if (!showVocabTooltip) {
                                        speak(vocabWord)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = LituturTertiary, modifier = Modifier.size(14.dp))
                            Text("[$vocabWord]", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTertiary)
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = showVocabTooltip && vocabWord != null) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LituturSurfaceHigh),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LituturTertiary)
                                        Text("Kamus Cilik: ${vocabWord.orEmpty()}", fontWeight = FontWeight.Bold, color = LituturTertiary)
                                    }
                                    IconButton(
                                        onClick = { showVocabTooltip = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = LituturTextVariant)
                                    }
                                }

                                Text(
                                    vocabMeaning,
                                    fontSize = 13.sp,
                                    color = LituturTextDark
                                )

                                Button(
                                    onClick = {
                                        speak("Dahan Pohon. Cabang kayu tempat daun, bunga, dan buah-buahan tumbuh di pohon rindang.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = LituturTertiary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dengar Bunyi Kata 🔊", fontSize = 11.sp, color = LituturTertiary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TactileBox(
                    modifier = Modifier.weight(1f),
                    backgroundColor = LituturSurfaceHigh,
                    shadowColor = LituturSurfaceVariant,
                    cornerRadius = 16.dp,
                    shadowDepth = 4.dp,
                    onClick = {
                        stopSpeaking()
                        onPreviousChapter()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, tint = LituturTextDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sebelumnya", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }
                }

                TactileBox(
                    modifier = Modifier.weight(1f),
                    backgroundColor = LituturPurple,
                    shadowColor = LituturPurpleDark,
                    cornerRadius = 16.dp,
                    shadowDepth = 4.dp,
                    onClick = {
                        stopSpeaking()
                        onNextChapter()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isLastChapter) "Mulai Kuis" else "Lanjut Cerita", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
