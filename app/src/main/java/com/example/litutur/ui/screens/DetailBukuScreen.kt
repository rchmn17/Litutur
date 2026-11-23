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
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*

@Composable
fun DetailBukuScreen(
    bookTitle: String = "Singa yang Baik Hati",
    onBackClick: () -> Unit = {},
    onStartReading: () -> Unit = {}
) {
    var isFavorite by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf("narrated") }

    Scaffold(
        topBar = {
            Surface(color = LituturSurface.copy(alpha = 0.95f), shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LituturSurfaceHigh)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = LituturTextDark)
                        }
                        Text("Detail Buku", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LituturSurfaceVariant)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Narasi", tint = LituturPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(LituturPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
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
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDX-opz_jVCmK7BHSgKcb5mUBEKuHkjwPBJuTOlupKaFV7qvy37MpggpQhFgjyXw4zUjpY_tLmSXTU48ZW6t9bxVwkMBGnSEcEaT31H5VV2n58rFJmutqmdLihzSHP_rA4SvwxTi4AJbzOWUUVDqSRuvvkjIfH3MDiSb6NvReYYfBP2xExf9wKFybQ1t_YAEQExC8lwPtk3BXknPfpXeXsLE0F8EXUNW_AAomtn9rBQ",
                    contentDescription = "Cover Buku Leo",
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

                    IconButton(
                        onClick = { isFavorite = !isFavorite },
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
                    Text("5 Bab Interaktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                            Text(bookTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("★ 4.9", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                                Text("(320 ulasan)", fontSize = 12.sp, color = LituturOutline)
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
                            Text("Empati & Kebaikan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturPrimary)
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
                                Text("Level 2 (Pemula)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                                Text("8 Menit Baca", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                            }
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
                    Text("+15 Koin Emas setelah kuis selesai! 🪙", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                }
            }
        }
    }
}
