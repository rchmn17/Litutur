package com.example.litutur.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BacaCeritaScreen(
    onBackClick: () -> Unit = {},
    onNextChapter: () -> Unit = {}
) {
    var showVocabTooltip by remember { mutableStateOf(false) }
    var showRoarEffect by remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(true) }

    LaunchedEffect(showRoarEffect) {
        if (showRoarEffect) {
            delay(1200)
            showRoarEffect = false
        }
    }

    Scaffold(
        topBar = {
            Surface(color = LituturSurface.copy(alpha = 0.95f), shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(LituturSurfaceHigh)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = LituturTextDark)
                        }
                        Text("Baca Cerita", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { isAudioPlaying = !isAudioPlaying },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LituturTertiaryFixed)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Audio", tint = LituturTertiary)
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
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LituturSurfaceHigh)
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuB6FTTn9EFp03Fep4lQx-iorVyxZPV9mghlg2RxxulF34oiHI4Y0dJ_rwZmp5yHq3xJQeTvGfCVNYzt0w0MTlsEcBeqP5MVVhCBO-Wa-dXPY8YGy7m4nK6WUzM3zNz2Q7AfXrfWQ_U9btLw8W6o-dQePn4CqB4oXa5Oi69_2y_JO6fF3_hS5jyJFmIRv4xZwJvg-vwZrJILqTbZ7el8em5U0F87yKY0LGVqnbxxzK1S",
                    contentDescription = "Leo si Singa dan Burung",
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
                        Text("Bab 2 dari 5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(LituturPrimaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Sedang Membaca", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                AnimatedVisibility(
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
                        Text("RAWRRR! 🦁✨", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable { showRoarEffect = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = LituturPrimaryContainer, modifier = Modifier.size(16.dp))
                        Text("Ketuk Leo untuk aumannya! 🐾", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(LituturSecondaryDim),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(LituturPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(LituturSurfaceVariant)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Meskipun tubuhnya paling besar di antara teman-temannya, Leo tidak pernah sombong. Ketika mendengar cicitan cemas sang burung pipit, ",
                        fontSize = 17.sp,
                        lineHeight = 28.sp,
                        color = LituturTextDark
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Leo segera melompat",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LituturSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LituturSecondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Text(" ke ", fontSize = 17.sp, color = LituturTextDark)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LituturTertiaryFixed)
                                .clickable { showVocabTooltip = !showVocabTooltip }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = LituturTertiary, modifier = Modifier.size(14.dp))
                            Text("[Dahan Pohon]", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTertiary)
                        }
                    }

                    Text(
                        text = "untuk membantunya lepas dengan selamat.",
                        fontSize = 17.sp,
                        lineHeight = 28.sp,
                        color = LituturTextDark
                    )

                    AnimatedVisibility(visible = showVocabTooltip) {
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
                                        Text("Kamus Cilik: Dahan Pohon", fontWeight = FontWeight.Bold, color = LituturTertiary)
                                    }
                                    IconButton(
                                        onClick = { showVocabTooltip = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = LituturTextVariant)
                                    }
                                }

                                Text(
                                    "Cabang kayu tempat daun, bunga, dan buah-buahan tumbuh di pohon rindang.",
                                    fontSize = 13.sp,
                                    color = LituturTextDark
                                )

                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = LituturTertiary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dengar Bunyi Kata", fontSize = 11.sp, color = LituturTertiary, fontWeight = FontWeight.Bold)
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
                    onClick = onBackClick
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
                    onClick = onNextChapter
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lanjut Cerita", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
