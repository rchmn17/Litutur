package com.example.litutur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.data.model.CeritaBukuModel
import com.example.litutur.data.model.StikerModel
import com.example.litutur.data.repository.TreeGrowthData
import com.example.litutur.data.remote.LituturAssets
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.components.rememberAssetUrl
import com.example.litutur.ui.theme.*

/** Emoji hiasan untuk chip kategori (nama kategori asli tetap dari data). */
private fun categoryChipLabel(category: String): String = when (category) {
    "Fabel" -> "🦁 Fabel"
    "Petualangan" -> "🚀 Petualangan"
    "Dongeng" -> "🧚 Dongeng"
    "Sains" -> "🔬 Sains"
    "Favoritku" -> "❤️ Favoritku"
    else -> category
}

@Composable
fun DashboardAnakScreen(
    childName: String = "Budi",
    childPoints: Int = 120,
    books: List<CeritaBukuModel> = emptyList(),
    categories: List<String> = listOf("Semua Cerita"),
    selectedCategoryIndex: Int = 0,
    isLoading: Boolean = false,
    treeGrowth: TreeGrowthData = TreeGrowthData(1, "Tunas Kecil 🌱", 0.5f, 25, "Bibit Hijau"),
    stickers: List<StikerModel> = emptyList(),
    dailyMissionProgress: Int = 0,
    dailyMissionTarget: Int = 2,
    dailyMissionReward: Int = 20,
    isDailyMissionClaimed: Boolean = false,
    onClaimDailyMission: () -> Unit = {},
    onCategorySelected: (Int) -> Unit = {},
    onRequestParentMode: () -> Unit = {},
    onNavigateToRead: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val unlockedStickers = stickers.count { it.isUnlocked }
    val missionDone = dailyMissionProgress.coerceAtMost(dailyMissionTarget)
    val missionClaimable = !isDailyMissionClaimed && dailyMissionProgress >= dailyMissionTarget
    val missionSubtitle = when {
        isDailyMissionClaimed -> "Misi selesai! Hebat sekali 🎉"
        missionClaimable -> "Misi tercapai! Ketuk untuk klaim hadiah 🎁"
        else -> "Baca ${dailyMissionTarget - dailyMissionProgress} cerita lagi yuk! ($missionDone/$dailyMissionTarget)"
    }

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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(LituturPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Face, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text("LITUTUR ANAK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturOutline)
                            Text("Halo, $childName! 🌟", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LituturSurfaceHigh)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = LituturSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$childPoints Poin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        }
                        IconButton(
                            onClick = onRequestParentMode,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LituturSurfaceVariant)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = "Kunci Orang Tua", tint = LituturTextVariant)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { onBottomTabSelected(0) },
                    icon = { Icon(Icons.Default.AutoStories, null) },
                    label = { Text("Beranda", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = LituturPurple)
                )
                NavigationBarItem(selected = false, onClick = { onBottomTabSelected(1) }, icon = { Icon(Icons.Default.Park, null) }, label = { Text("Pohon Kisah") })
                NavigationBarItem(selected = false, onClick = { onBottomTabSelected(2) }, icon = { Icon(Icons.Default.Stars, null) }, label = { Text("Album") })
                NavigationBarItem(selected = false, onClick = { onBottomTabSelected(3) }, icon = { Icon(Icons.Default.MilitaryTech, null) }, label = { Text("Prestasi") })
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
            TactileBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LituturSurfaceHigh,
                shadowColor = LituturSurfaceVariant,
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LituturPrimaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PETUALANGAN BARU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Siap berpetualang hari ini?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LituturTextDark, lineHeight = 26.sp)
                        Text("Buka cerita seru dan dapatkan stiker ajaib! ✨", fontSize = 12.sp, color = LituturTextVariant)
                    }

                    AsyncImage(
                        model = rememberAssetUrl(LituturAssets.DASHBOARD_MASCOT.sourceUrl),
                        contentDescription = "Mascot Anak",
                        modifier = Modifier.size(90.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TactileBox(
                    modifier = Modifier.weight(1f),
                    backgroundColor = LituturSecondaryContainer,
                    shadowColor = LituturSecondaryDim,
                    onClick = { onBottomTabSelected(1) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Park, contentDescription = null, tint = LituturSecondary)
                            }
                            Text("Tahap ${treeGrowth.stage}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturSecondary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Pohon Kisahku 🌳", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text(treeGrowth.stageLabel, fontSize = 12.sp, color = LituturTextVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                TactileBox(
                    modifier = Modifier.weight(1f),
                    backgroundColor = LituturTertiaryFixed,
                    shadowColor = LituturTertiaryDim,
                    onClick = { onBottomTabSelected(2) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = LituturTertiary)
                            }
                            Text("⭐ $unlockedStickers/${stickers.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturTertiary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Album Stiker ⭐", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text("Terkumpul $unlockedStickers", fontSize = 12.sp, color = LituturTextVariant)
                    }
                }
            }

            TactileBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (missionClaimable) LituturSecondaryContainer else Color.White,
                shadowColor = if (missionClaimable) LituturSecondaryDim else LituturSurfaceVariant,
                onClick = onClaimDailyMission
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(LituturPrimaryContainer.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = LituturPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Misi Hari Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                                Text(missionSubtitle, fontSize = 12.sp, color = LituturTextVariant)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (missionClaimable) Color.White else LituturSecondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isDailyMissionClaimed) Icons.Default.CheckCircle else Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = LituturSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                when {
                                    isDailyMissionClaimed -> "Diklaim"
                                    missionClaimable -> "Klaim +$dailyMissionReward"
                                    else -> "+$dailyMissionReward Poin"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LituturSecondary
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { missionDone.toFloat() / dailyMissionTarget.coerceAtLeast(1).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = LituturPrimaryContainer,
                        trackColor = LituturSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEachIndexed { index, cat ->
                    val isSelected = selectedCategoryIndex == index
                    TactileBox(
                        backgroundColor = if (isSelected) LituturPurple else Color.White,
                        shadowColor = if (isSelected) LituturPurpleDark else LituturSurfaceVariant,
                        cornerRadius = 50.dp,
                        shadowDepth = 3.dp,
                        onClick = { onCategorySelected(index) }
                    ) {
                        Text(
                            text = categoryChipLabel(cat),
                            color = if (isSelected) Color.White else LituturTextVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            when {
                isLoading && books.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = LituturPurple)
                    }
                }

                books.isEmpty() -> {
                    Text(
                        "Belum ada cerita di kategori ini. Coba kategori lain ya! 📚",
                        fontSize = 13.sp,
                        color = LituturTextVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    )
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        for (chunk in books.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (book in chunk) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        BookCardItem(book = book, onReadClick = { onNavigateToRead(book.title) })
                                    }
                                }
                                // Jaga lebar kartu tetap sama bila baris terakhir hanya berisi 1 buku
                                if (chunk.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookCardItem(book: CeritaBukuModel, onReadClick: () -> Unit) {
    val badgeColor = when (book.badgeText) {
        "Favorit" -> LituturPrimaryContainer
        "Baru" -> LituturSecondaryContainer
        else -> LituturPrimaryContainer
    }
    TactileBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shadowColor = LituturSurfaceVariant,
        cornerRadius = 16.dp,
        onClick = onReadClick
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (!book.badgeText.isNullOrBlank()) {
                    Text(
                        text = book.badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(50))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = book.level,
                    color = LituturTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(book.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = LituturTextDark)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("★ ${book.rating}", fontSize = 11.sp, color = LituturPrimaryContainer, fontWeight = FontWeight.Bold)
                    Text("•", fontSize = 11.sp, color = LituturOutline)
                    Text("${book.durationMinutes} mnt", fontSize = 11.sp, color = LituturTextVariant)
                }

                TactileBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LituturSurfaceVariant,
                    shadowColor = LituturSurfaceHigh,
                    cornerRadius = 10.dp,
                    shadowDepth = 2.dp,
                    onClick = onReadClick
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Baca", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
