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
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*

data class QuizOption(val id: String, val letter: String, val text: String)

@Composable
fun KuisPemahamanScreen(
    onBackClick: () -> Unit = {},
    onQuizComplete: () -> Unit = {}
) {
    var selectedOptionId by remember { mutableStateOf("B") }

    val options = listOf(
        QuizOption("A", "A", "Mengabaikannya dan terus berjalan santai"),
        QuizOption("B", "B", "Memanjat dahan dengan hati-hati untuk menolongnya"),
        QuizOption("C", "C", "Menyuruh hewan lain yang lebih kecil"),
        QuizOption("D", "D", "Menertawakan burung pipit tersebut")
    )

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
                    Text("Pertanyaan 2 dari 3", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                    Text("+10 Poin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(50)).background(LituturPurpleDark))
                Box(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(50)).background(LituturPurple))
                Box(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(50)).background(LituturSurfaceVariant))
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDhvbfzD6pIZkNMTsOezV3_LzXyfx8TFzRS_tLmn16Mk5iU-bjElxciotT-mxoFYhiPwHlKUViexV5oUqJgrUhPmsqs_pB-tw5YoCAYweErVx7x8op3k0Y4Olr2BYaK4QZAvBt0mLTZl2uV_jarIeXhukLbb-YHfRWvTZIPxLwp8if26gztCpIwvp32gt6gJl8YPzYvFHemrxxq4uncOm9ARmeiGeNEhHdtxRP_aX68",
                            contentDescription = "Leo berpikir",
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

                    Text(
                        "Apa yang dilakukan Leo ketika melihat burung pipit kecil kesulitan di atas pohon?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = LituturTextDark
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { opt ->
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
                        onClick = { selectedOptionId = opt.id }
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
                                    Text(opt.letter, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else LituturTertiary)
                                }
                                Text(opt.text, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
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
                        "Petunjuk Ceria: Ingat pesan Leo tentang rasa peduli dan saling tolong-menolong!",
                        fontSize = 12.sp,
                        color = LituturTextDark,
                        lineHeight = 16.sp
                    )
                }
            }

            TactileBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LituturPurple,
                shadowColor = LituturPurpleDark,
                cornerRadius = 16.dp,
                shadowDepth = 4.dp,
                onClick = onQuizComplete
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Periksa Jawaban & Lanjut", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Stars, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
