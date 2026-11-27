package com.example.litutur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.data.model.CeritaBukuModel
import com.example.litutur.ui.components.ChildDetailTopBar
import com.example.litutur.ui.components.ChildIconAction
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.components.rememberLituturSpeaker
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.litutur.ui.theme.*

@Composable
fun DetailBukuScreen(
    bookTitle: String = "Singa yang Baik Hati",
    book: CeritaBukuModel? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onStartReading: () -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf("narrated") }

    // Buku belum selesai dimuat: tampilkan loading
    if (book == null) {
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

    val hasNarration = book.chapters.any { !it.audioUrl.isNullOrBlank() }
    val speaker = rememberLituturSpeaker()
    val detailContext = LocalContext.current

    Scaffold(
        topBar = {
            ChildDetailTopBar(title = "Detail Buku", onBack = {
                speaker.stop()
                onBackClick()
            }) {
                ChildIconAction(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Dengarkan sinopsis",
                    tint = LituturPrimary,
                    onClick = {
                        val text = "${book.title}. ${book.sinopsis.ifBlank { book.moralCerita }}"
                        if (!speaker.speak(text)) {
                            Toast.makeText(detailContext, "Suara sedang disiapkan, coba lagi sebentar ya", Toast.LENGTH_SHORT).show()
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
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LituturSurfaceHigh)
            ) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = "Cover ${book.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasNarration) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.95f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = LituturPrimary, modifier = Modifier.size(16.dp))
                            Text("ADA SUARA NARASI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturPrimary)
                        }
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isFavorite) LituturPrimaryContainer else Color.White.copy(alpha = 0.95f))
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Simpan Favorit",
                            tint = if (isFavorite) Color.White else LituturPrimary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = LituturSecondary, modifier = Modifier.size(16.dp))
                    Text("${book.chapters.size} Bab Interaktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pilih Mode Baca", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text("✨ Otomatis Sesuai Usia", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturPrimary)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LituturSurfaceHigh)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedMode == "narrated") Color.White else Color.Transparent)
                                .clickable { selectedMode = "narrated" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Didengar 🎧",
                                fontSize = 13.sp,
                                fontWeight = if (selectedMode == "narrated") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedMode == "narrated") LituturPrimary else LituturTextVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedMode == "self") Color.White else Color.Transparent)
                                .clickable { selectedMode = "self" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Baca Sendiri 📖",
                                fontSize = 13.sp,
                                fontWeight = if (selectedMode == "self") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedMode == "self") LituturPrimary else LituturTextVariant
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(book.title.ifBlank { bookTitle }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("★ ${book.rating}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                                if (!book.badgeText.isNullOrBlank()) {
                                    Text("• ${book.badgeText}", fontSize = 12.sp, color = LituturOutline)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LituturPrimaryContainer.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = LituturPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(book.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturPrimary)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LituturSurfaceHigh)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Tingkat Membaca", fontSize = 11.sp, color = LituturTextVariant)
                                Text(book.level, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LituturSoftCream)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Estimasi Durasi", fontSize = 11.sp, color = LituturTextVariant)
                                Text("${book.durationMinutes} Menit Baca", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                            }
                        }
                    }

                    if (book.sinopsis.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tentang Cerita", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturOutline)
                            Text(book.sinopsis, fontSize = 14.sp, lineHeight = 21.sp, color = LituturTextDark)
                        }
                    }

                    if (book.moralCerita.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LituturTertiaryFixed)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LituturTertiary, modifier = Modifier.size(18.dp))
                            Text(
                                "Pesan moral: ${book.moralCerita}",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = LituturTextDark
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TactileBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LituturPrimary,
                    shadowColor = LituturPrimaryShadow,
                    cornerRadius = 18.dp,
                    shadowDepth = 5.dp,
                    onClick = onStartReading
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mulai Petualangan Membaca! 🚀",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LituturSoftCream)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LituturGoldText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dapatkan hingga +25 Poin setelah kuis selesai! 🪙", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                }
            }
        }
    }
}
