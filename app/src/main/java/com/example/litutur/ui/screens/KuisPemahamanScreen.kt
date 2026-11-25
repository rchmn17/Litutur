package com.example.litutur.ui.screens

import androidx.compose.foundation.background
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
import com.example.litutur.data.model.SoalKuisModel
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*

@Composable
fun KuisPemahamanScreen(
    bookTitle: String = "Singa yang Baik Hati",
    question: SoalKuisModel? = null,
    questionIndex: Int = 0,
    totalQuestions: Int = 0,
    selectedOptionId: String? = null,
    illustrationUrl: String? = null,
    hint: String = "",
    onOptionSelected: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val screenBackground = Brush.verticalGradient(listOf(LituturBgStart, LituturBgMid, LituturBgEnd))

    // Buku ini belum punya soal kuis
    if (totalQuestions == 0) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Kuis belum tersedia 📝", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Cerita \"$bookTitle\" belum punya soal. Yuk baca cerita lainnya!", fontSize = 13.sp, color = LituturTextVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = LituturPurple)
            ) {
                Text("Kembali", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Soal sedang dimuat
    if (question == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(screenBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = LituturPurple)
        }
        return
    }

    val isLastQuestion = questionIndex >= totalQuestions - 1
    val canSubmit = selectedOptionId != null

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
                        Text("Kuis Pemahaman", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                                .background(LituturTertiaryFixed)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Suara Soal", tint = LituturTertiary)
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(LituturPurple),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LituturLightPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = LituturPurpleDark, modifier = Modifier.size(18.dp))
                    }
                    Text("Pertanyaan ${questionIndex + 1} dari $totalQuestions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LituturSoftCream)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LituturGoldText, modifier = Modifier.size(16.dp))
                    Text("Hingga +25 Poin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalQuestions) { i ->
                    val segmentColor = when {
                        i < questionIndex -> LituturPurpleDark
                        i == questionIndex -> LituturPurple
                        else -> LituturSurfaceVariant
                    }
                    Box(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(50)).background(segmentColor))
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!illustrationUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = illustrationUrl,
                                contentDescription = "Ilustrasi soal",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LituturPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fokus Membaca", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                            }
                        }
                    }

                    Text(
                        question.questionText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = LituturTextDark
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEach { opt ->
                    val isSelected = selectedOptionId == opt.id
                    val bgColor = if (isSelected) LituturPurple else LituturSurfaceHigh
                    val shadowColor = if (isSelected) LituturPurpleDark else LituturSurfaceVariant
                    val textColor = if (isSelected) Color.White else LituturTextDark

                    TactileBox(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = bgColor,
                        shadowColor = shadowColor,
                        cornerRadius = 16.dp,
                        shadowDepth = 3.dp,
                        onClick = { onOptionSelected(opt.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) LituturPurpleDark else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(opt.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else LituturTertiary)
                                }
                                Text(opt.optionText, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(LituturPurpleDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (hint.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LituturSurfaceHigh)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EmojiObjects, contentDescription = null, tint = LituturPrimaryContainer)
                        }
                        Text(
                            "Petunjuk Ceria: $hint",
                            fontSize = 12.sp,
                            color = LituturTextDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            TactileBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (canSubmit) LituturPurple else LituturSurfaceVariant,
                shadowColor = if (canSubmit) LituturPurpleDark else LituturSurfaceHigh,
                cornerRadius = 16.dp,
                shadowDepth = 4.dp,
                onClick = { if (canSubmit) onSubmit() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isLastQuestion) "Periksa Jawaban & Selesai" else "Periksa Jawaban & Lanjut",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSubmit) Color.White else LituturTextVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Stars, contentDescription = null, tint = if (canSubmit) Color.White else LituturTextVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
