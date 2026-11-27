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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QuestionAnswer
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.litutur.data.model.ParentMissionEntity
import com.example.litutur.ui.components.ParentTopBar
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.LituturTheme
import com.example.litutur.ui.viewmodel.ParentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentMissionsScreen(
    uiState: ParentUiState = ParentUiState(),
    onSelectChild: (Int) -> Unit = {},
    onToggleMission: (ParentMissionEntity) -> Unit = {},
    onAddCustomMission: (title: String, description: String) -> Unit = { _, _ -> }
) {
    var showAddMissionDialog by remember { mutableStateOf(false) }

    if (showAddMissionDialog) {
        AddMissionDialog(
            onDismiss = { showAddMissionDialog = false },
            onConfirm = { title, desc ->
                onAddCustomMission(title, desc)
                showAddMissionDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            ParentTopBar(title = "Misi & Bonding")
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
            if (uiState.children.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.children) { idx, child ->
                            FilterChip(
                                selected = idx == uiState.selectedChildIndex,
                                onClick = { onSelectChild(idx) },
                                label = { Text("Anak: ${child.name}") }
                            )
                        }
                    }
                }
            }

            // 1. Weekly Progress Overview Card
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
                            Column {
                                Text(
                                    text = "Aktivitas Bersama Anak",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Minggu ini: ${uiState.completedMissionsCount}/${uiState.totalMissionsCount} selesai",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${uiState.totalMissionPoints} Poin",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { uiState.missionProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = Color(0xFF16A34A),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // 2. Offline Bonding Prompts Card (Problem Solving)
            uiState.bondingPrompt?.let { prompt ->
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFCA8A04)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (prompt.isFromRecentReading) {
                                        "Offline Bonding Prompt (Bacaan Terbaru)"
                                    } else {
                                        "Offline Bonding Prompt (Rekomendasi Minat)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF854D0E)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = prompt.sourceStoryTitle,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prompt.summaryText,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            prompt.discussionQuestions.forEach { q ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QuestionAnswer,
                                        contentDescription = null,
                                        tint = Color(0xFFA16207),
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = q,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Mission Checklist Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Checklist Misi Pendampingan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Ketuk untuk centang",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            items(uiState.missions, key = { it.id }) { mission ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mission.isCompleted) Color(0xFFF0FDF4) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (mission.isCompleted) Color(0xFF86EFAC) else Color(0xFFE2E8F0),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onToggleMission(mission) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mission.isCompleted) Color(0xFF16A34A) else Color(0xFFF1F5F9))
                                .border(
                                    1.dp,
                                    if (mission.isCompleted) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (mission.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selesai",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mission.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (mission.isCompleted) Color(0xFF166534) else Color(0xFF0F172A),
                                textDecoration = if (mission.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (mission.isCompleted && !mission.completedDay.isNullOrBlank()) {
                                    "Selesai • ${mission.completedDay} • +${mission.pointsReward} Poin"
                                } else {
                                    "${mission.category} • +${mission.pointsReward} Poin"
                                },
                                fontSize = 12.sp,
                                color = if (mission.isCompleted) Color(0xFF15803D) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // 4. Add Custom Mission CTA
            item {
                OutlinedButton(
                    onClick = { showAddMissionDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CoralOrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tambah Misi Sendiri",
                        fontWeight = FontWeight.Bold,
                        color = CoralOrangePrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMissionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Misi Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Misi (mis. Baca 10 menit)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Catatan Singkat (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title, description)
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrangePrimary)
            ) {
                Text("Simpan Misi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ParentMissionsScreenPreview() {
    LituturTheme {
        ParentMissionsScreen()
    }
}
