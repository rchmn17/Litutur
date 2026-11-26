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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.data.model.StikerModel
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*
import com.example.litutur.ui.viewmodel.QuizSaveStatus

@Composable
fun KuisSelesaiScreen(
    childName: String = "Jagoan",
    bookTitle: String = "Singa yang Baik Hati",
    quizScore: Int = 100,
    earnedPoints: Int = 25,
    correctAnswers: Int = 0,
    totalQuestions: Int = 0,
    durationSeconds: Int = 0,
    totalPoints: Int = 0,
    newlyUnlockedStickers: List<StikerModel> = emptyList(),
    nextLockedSticker: StikerModel? = null,
    saveStatus: QuizSaveStatus = QuizSaveStatus.IDLE,
    onClaimReward: () -> Unit = {},
    onReadMoreBooks: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onReadMoreBooks,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = LituturTextDark)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFFCF0FF), Color(0xFFF7E8FF), Color(0xFFEDD9FC))))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(LituturLightPurple.copy(alpha = 0.6f))
                )
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBe4n2jzIROOHNosEZFY_unI3-bEkGGM1RYeJrf0VY8DWBhCGEL_e-VOkYCk9MemXubxJZFcAGES695VbeiLp1i_eY8Sz_WlpFUQH_MGY2MhwBVnYvF-1Bqaji-p3-OMAQRSd3ybEEW57chUqjG49qVqrTtpP2_OHuEbi9nFBQaYvi7ly9SRqdwuqPb5xas42-pV95RrnkkhoUZbCKpHqpJngGeo6X44Tnbztyl1RES",
                    contentDescription = "Maskot & Trofi Emas",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = LituturPurpleDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("MISI SELESAI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturPurpleDark)
                }

                Text("Luar Biasa, $childName! 🎉", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                Text(
                    "Kamu menyelesaikan \"$bookTitle\" dengan skor $quizScore% dan mendapat +$earnedPoints Poin!",
                    fontSize = 13.sp,
                    color = LituturTextVariant,
                    textAlign = TextAlign.Center
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LituturGoldBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LituturGoldText, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("+$earnedPoints Koin Emas Baru! 🪙", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Total Tabungan:", fontSize = 12.sp, color = LituturTextVariant)
                            Text("$totalPoints Koin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturPurpleDark)
                        }
                        SaveStatusLabel(saveStatus)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LituturBgStart)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hasil Kuis: $correctAnswers/$totalQuestions Benar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text("$quizScore% ${scoreLabel(quizScore)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturGoldText)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Waktu Selesai:", fontSize = 13.sp, color = LituturTextVariant)
                        Text(formatDuration(durationSeconds), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LituturLightPurple.copy(alpha = 0.5f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = LituturPurpleDark)
                        val stickerText = when {
                            newlyUnlockedStickers.isNotEmpty() ->
                                "Stiker ${newlyUnlockedStickers.joinToString(", ") { it.name }} Terbuka! 🎉"
                            nextLockedSticker != null ->
                                "Stiker berikutnya: ${nextLockedSticker.name} • ${nextLockedSticker.unlockCriteria}"
                            else -> "Semua stiker sudah terkumpul! 🏆"
                        }
                        Text(stickerText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TactileBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LituturPurple,
                    shadowColor = LituturPurpleDark,
                    cornerRadius = 16.dp,
                    shadowDepth = 4.dp,
                    onClick = onClaimReward
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Park, contentDescription = null, tint = LituturSecondaryContainer)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Klaim Hadiah & Siram Pohon 🌳", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                TactileBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    shadowColor = LituturSurfaceVariant,
                    cornerRadius = 16.dp,
                    shadowDepth = 3.dp,
                    onClick = onReadMoreBooks
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = LituturPurple)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Baca Cerita Lainnya 📚", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveStatusLabel(status: QuizSaveStatus) {
    val (text, color) = when (status) {
        QuizSaveStatus.SAVING -> "Menyimpan progres..." to LituturTextVariant
        QuizSaveStatus.SAVED -> "Progres tersimpan ✓" to LituturSecondary
        QuizSaveStatus.LOCAL_ONLY -> "Mode tamu: progres tidak disimpan ke akun" to LituturTextVariant
        QuizSaveStatus.FAILED -> "Gagal menyimpan progres, cek koneksi" to Color(0xFFB3261E)
        QuizSaveStatus.IDLE -> return
    }
    Text(text, fontSize = 11.sp, color = color)
}

private fun scoreLabel(score: Int): String = when {
    score >= 100 -> "Sempurna! ⭐"
    score >= 70 -> "Hebat! 👍"
    else -> "Terus Berlatih! 💪"
}

private fun formatDuration(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "-"
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes == 0 -> "$seconds Detik"
        seconds == 0 -> "$minutes Menit"
        else -> "$minutes Menit $seconds Detik"
    }
}
