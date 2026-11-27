package com.example.litutur.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.litutur.ui.components.ParentTopBar
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.LituturTheme
import com.example.litutur.ui.viewmodel.ParentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAnalyticsScreen(
    uiState: ParentUiState = ParentUiState(),
    onSelectChild: (Int) -> Unit = {},
    onRecordReading: (title: String, category: String, minutes: Int, quizScore: Int) -> Unit = { _, _, _, _ -> }
) {
    val analysis = uiState.literacyAnalysis
    val activeChild = uiState.selectedChild
    val dayLabels = listOf("S", "S", "R", "K", "J", "S", "M")

    var showAddLogDialog by remember { mutableStateOf(false) }

    if (showAddLogDialog) {
        RecordReadingDialog(
            onDismiss = { showAddLogDialog = false },
            onConfirm = { title, category, minutes, score ->
                onRecordReading(title, category, minutes, score)
                showAddLogDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            ParentTopBar(title = "Monitoring Literasi") {
                TextButton(onClick = { showAddLogDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CoralOrangePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catat Baca", color = CoralOrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        containerColor = Color(0xFFF8F9FD)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Child selector chips
            if (uiState.children.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.children) { idx, child ->
                            FilterChip(
                                selected = idx == uiState.selectedChildIndex,
                                onClick = { onSelectChild(idx) },
                                label = {
                                    Text(
                                        text = "${child.name} (${child.ageRange})",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 1. Automatic Literacy Status Banner (Problem Solving Conclusion)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF059669)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Status Literasi: ${analysis.statusLabel}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFF059669))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${analysis.literacyPercent}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = analysis.statusFeedback,
                            fontSize = 13.sp,
                            color = Color(0xFF064E3B)
                        )
                    }
                }
            }

            // 2. Daily Reading Bar Chart (S-S-R-K-J-S-M)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = CoralOrangePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Aktivitas Membaca Mingguan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${analysis.totalStoriesRead} cerita • ${analysis.totalDurationMinutes} mnt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val maxCount = (analysis.dailyCounts.maxOrNull() ?: 1).coerceAtLeast(1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            analysis.dailyCounts.forEachIndexed { idx, count ->
                                val barFraction = if (count == 0) 0.08f else (count.toFloat() / maxCount.toFloat()).coerceIn(0.18f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.height(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (count > 0) {
                                            Text(
                                                text = "$count",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CoralOrangePrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(88.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height((88 * barFraction).dp)
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                .background(if (count > 0) CoralOrangePrimary else Color(0xFFE2E8F0))
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = dayLabels.getOrElse(idx) { "" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (count > 0) CoralOrangePrimary else Color(0xFF475569),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Interest Radar / Category Distribution
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pemetaan Minat Baca (${activeChild?.name ?: "Anak"})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (analysis.interestDistribution.isEmpty()) {
                            Text(
                                text = "Belum ada data minat baca. Catat bacaan pertama untuk memetakan minat anak.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            analysis.interestDistribution.entries
                                .sortedByDescending { it.value }
                                .forEach { (category, ratio) ->
                                    val percent = (ratio * 100).toInt()
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                            Text("$percent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { ratio.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(7.dp)
                                                .clip(RoundedCornerShape(50.dp)),
                                            color = Color(0xFF7C3AED),
                                            trackColor = Color(0xFFF1F5F9)
                                        )
                                    }
                                }
                        }
                    }
                }
            }

            // 4. Reading History List
            item {
                Text(
                    text = "Riwayat Bacaan & Kuis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            }

            if (uiState.readingLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Belum ada riwayat bacaan.", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gunakan tombol '+ Catat Baca' di kanan atas untuk mencatat aktivitas bacaan bersama.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(uiState.readingLogs) { log ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF7ED)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = CoralOrangePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = log.storyTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = log.category.ifBlank { "Dongeng" },
                                            fontSize = 11.sp,
                                            color = Color(0xFF7C3AED),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(" • ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("${log.durationMinutes} mnt", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECFDF5))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Kuis ${log.quizScore}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordReadingDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, minutes: Int, quizScore: Int) -> Unit
) {
    var title by remember { mutableStateOf("Kancil & Buaya") }
    var category by remember { mutableStateOf("Fabel & Dongeng") }
    var minutesText by remember { mutableStateOf("15") }
    var scoreText by remember { mutableStateOf("90") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Aktivitas Bacaan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Cerita") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (mis. Hewan, Sains)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minutesText = it },
                        label = { Text("Durasi (mnt)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) scoreText = it },
                        label = { Text("Skor Kuis") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = minutesText.toIntOrNull()?.coerceIn(1, 180) ?: 10
                    val s = scoreText.toIntOrNull()?.coerceIn(0, 100) ?: 85
                    if (title.isNotBlank()) {
                        onConfirm(title, category.ifBlank { "Dongeng" }, m, s)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrangePrimary)
            ) {
                Text("Simpan ke Firestore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ParentAnalyticsScreenPreview() {
    LituturTheme {
        ParentAnalyticsScreen()
    }
}
