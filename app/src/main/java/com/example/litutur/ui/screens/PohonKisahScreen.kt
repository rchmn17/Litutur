package com.example.litutur.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.data.model.StikerModel
import com.example.litutur.data.repository.TREE_STAGE_MIN_POINTS
import com.example.litutur.data.repository.TreeGrowthData
import com.example.litutur.ui.components.ChildBottomBar
import com.example.litutur.ui.components.ChildHomeTopBar
import com.example.litutur.ui.components.ChildTab
import com.example.litutur.ui.components.PohonIllustration
import com.example.litutur.ui.components.TREE_FRUIT_SIZE_FRACTION
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.components.treeFruitAnchors
import com.example.litutur.ui.theme.*

private data class TreeStageInfo(
    val name: String,
    val emoji: String,
    val description: String
)

/** Urutan = tahap 1..4. Ambang poin ada di [TREE_STAGE_MIN_POINTS]. */
private val treeStages = listOf(
    TreeStageInfo("Tunas Kecil", "🌱", "Tunas mungil mulai tumbuh berkat awal petualangan membacamu!"),
    TreeStageInfo("Bibit Hijau", "🌿", "Daunmu mulai rimbun, dan pohonmu mulai berbuah stiker pertama!"),
    TreeStageInfo("Pohon Muda", "🌳", "Batangmu makin kokoh dan cabangnya penuh buah stiker."),
    TreeStageInfo("Pohon Rimbun Megah", "🍎", "Pohonmu mekar sempurna dan berbuah paling banyak!")
)

@Composable
fun PohonKisahScreen(
    childName: String = "Sahabat Kecil",
    childPoints: Int = 0,
    storiesCount: Int = 0,
    treeGrowth: TreeGrowthData = TreeGrowthData(1, "Tunas Kecil 🌱", 0.5f, 25, "Bibit Hijau"),
    stickers: List<StikerModel> = emptyList(),
    isWateredToday: Boolean = false,
    waterRewardPoints: Int = 5,
    onWaterTree: () -> Unit = {},
    onRequestParentMode: () -> Unit = {},
    onNavigateToAlbum: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var showWaterCelebration by remember { mutableStateOf(false) }

    val isMaxStage = treeGrowth.pointsToNext <= 0
    val stage = treeGrowth.stage.coerceIn(1, treeStages.size)
    val stageInfo = treeStages[stage - 1]

    // --- Buah = stiker yang SUDAH didapat anak ---
    // Jumlah buah yang boleh tumbuh bertambah di setiap tahap (0, 2, 4, 6).
    val anchors = remember(stage) { treeFruitAnchors(stage) }
    val unlockedStickers = stickers.filter { it.isUnlocked }
    val fruitStickers = unlockedStickers.take(anchors.size)
    val emptyBuds = anchors.size - fruitStickers.size            // kuncup yang belum terisi stiker
    val waitingStickers = (unlockedStickers.size - anchors.size).coerceAtLeast(0)
    val nextStageNewFruits =
        if (stage < treeStages.size) treeFruitAnchors(stage + 1).size - anchors.size else 0

    val fruitHint: String? = when {
        stage == 1 ->
            "🍎 Pohon belum berbuah. Capai Tahap 2 (${TREE_STAGE_MIN_POINTS[1]} poin) agar mulai berbuah stiker!"
        emptyBuds > 0 ->
            "🌸 $emptyBuds kuncup menunggu stiker baru. Selesaikan cerita atau buka Kotak Misterius!"
        waitingStickers > 0 ->
            "🍎 $waitingStickers stiker lagi menunggu giliran berbuah di tahap berikutnya."
        nextStageNewFruits > 0 ->
            "🍎 Naik ke tahap berikutnya untuk menambah $nextStageNewFruits buah baru!"
        else -> null
    }

    Scaffold(
        topBar = {
            ChildHomeTopBar(
                title = "Pohon Kisah $childName",
                coins = childPoints,
                onRequestParentMode = onRequestParentMode,
                label = "TAMAN BACA",
                icon = Icons.Filled.Park
            )
        },
        bottomBar = {
            ChildBottomBar(selected = ChildTab.TREE, onTabSelected = onBottomTabSelected)
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
            // Segmented Tab Switcher (Pohon Kisah vs Album Stiker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LituturSurfaceHigh)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LituturPurple)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌳 Pohon Kisahku", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigateToAlbum() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐ Album Stiker", fontSize = 13.sp, color = LituturTextVariant)
                }
            }

            // ---------- Pohon + buah stiker ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFCFECFB), Color(0xFFF1F8E9))))
            ) {
                // Kotak persegi di tengah: gambar pohon & titik buah memakai koordinat yang sama
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                ) {
                    PohonIllustration(stage = stage, modifier = Modifier.fillMaxSize(), withSky = false)

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val fruitSize = maxWidth * TREE_FRUIT_SIZE_FRACTION
                        anchors.forEachIndexed { index, anchor ->
                            val sticker = fruitStickers.getOrNull(index)
                            StickerFruit(
                                sticker = sticker,
                                size = fruitSize,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(
                                        x = maxWidth * anchor.x - fruitSize / 2,
                                        y = maxHeight * anchor.y - fruitSize / 2
                                    ),
                                onClick = {
                                    val message = if (sticker != null) {
                                        if (sticker.soundText.isBlank()) sticker.name
                                        else "${sticker.name} — ${sticker.soundText}"
                                    } else {
                                        "🌸 Kuncup ini menunggu stiker baru!"
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // Subtle gradient overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.35f))
                            )
                        )
                )

                // Top badges over the tree
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            "🌱 $storiesCount Dongeng Selesai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LituturPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = LituturSecondaryContainer,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            "${stageInfo.emoji} Tahap $stage/${treeStages.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LituturSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Bottom badge over the tree: Current Stage Name
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        "${stageInfo.emoji} ${stageInfo.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LituturPurple,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // ---------- Tahapan pertumbuhan (dengan ilustrasi tiap level) ----------
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = LituturPrimary)
                            Text("Tahapan Pertumbuhan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        }
                        Text("Tahap $stage dari ${treeStages.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturPurple)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        treeStages.forEachIndexed { index, info ->
                            val level = index + 1
                            val isReached = stage >= level
                            val isCurrent = stage == level
                            val fruitSlots = treeFruitAnchors(level).size

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isCurrent) 3.dp else 1.dp,
                                            color = if (isCurrent) LituturPurple else LituturSurfaceVariant,
                                            shape = CircleShape
                                        )
                                ) {
                                    PohonIllustration(stage = level, modifier = Modifier.fillMaxSize())
                                    if (!isReached) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.White.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = "Terkunci", tint = LituturOutline, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Text(
                                    info.name.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) LituturPurple else LituturTextVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "${TREE_STAGE_MIN_POINTS[index]}+ Poin",
                                    fontSize = 9.sp,
                                    color = LituturOutline,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    if (fruitSlots == 0) "Belum berbuah" else "🍎 $fruitSlots buah",
                                    fontSize = 9.sp,
                                    color = if (isReached) LituturSecondary else LituturOutline,
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (index < treeStages.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.35f)
                                        .padding(top = 27.dp)
                                        .height(2.dp)
                                        .background(if (stage > level) LituturPurple else LituturSurfaceVariant)
                                )
                            }
                        }
                    }

                    Surface(shape = RoundedCornerShape(12.dp), color = LituturSurfaceHigh, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stageInfo.description, fontSize = 12.sp, color = LituturTextDark, lineHeight = 17.sp)
                            if (!isMaxStage) {
                                Text(
                                    "✨ Kumpulkan ${treeGrowth.pointsToNext} poin lagi untuk menjadi ${treeGrowth.nextStageLabel}!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LituturSecondary
                                )
                            } else {
                                Text(
                                    "🌟 Hebat! Pohonmu sudah tumbuh paling besar!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LituturPurple
                                )
                            }
                            if (fruitHint != null) {
                                Text(fruitHint, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturPrimary)
                            }
                        }
                    }
                }
            }

            // ---------- Ringkasan ----------
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "📖 Dongeng",
                    value = "$storiesCount Selesai",
                    valueColor = LituturTextDark
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "🍎 Buah Stiker",
                    value = if (anchors.isEmpty()) "Belum berbuah" else "${fruitStickers.size} / ${anchors.size} Buah",
                    valueColor = LituturSecondary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "💧 Kondisi",
                    value = if (isWateredToday) "Sangat Segar" else "Perlu Air",
                    valueColor = if (isWateredToday) LituturPrimary else LituturTertiary
                )
            }

            // ---------- Siram pohon ----------
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = LituturTertiary)
                            Text("Siram Pohon Hari Ini", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        }
                        if (isWateredToday) {
                            Text("🌸 Segar!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturPrimary)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                if (isMaxStage) "Pohon sudah tumbuh maksimal!"
                                else "${(treeGrowth.progress * 100).toInt()}% Menuju ${treeGrowth.nextStageLabel}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LituturTextDark
                            )
                            Text(
                                if (isMaxStage) "🎉" else "${treeGrowth.pointsToNext} Poin Lagi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LituturSecondary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { treeGrowth.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)),
                            color = LituturSecondary,
                            trackColor = LituturSurfaceHigh
                        )
                    }

                    // Water celebration message
                    AnimatedVisibility(
                        visible = showWaterCelebration,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = LituturSecondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💧", fontSize = 18.sp)
                                Text(
                                    "Segar sekali! Pohonmu tersenyum ceria dan mendapatkan nutrisi pertumbuhan!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LituturSecondary
                                )
                            }
                        }
                    }

                    TactileBox(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isWateredToday) LituturSecondary else LituturPurple,
                        shadowColor = if (isWateredToday) LituturSecondaryDim else LituturPurpleDark,
                        cornerRadius = 14.dp,
                        onClick = {
                            if (!isWateredToday) {
                                onWaterTree()
                                showWaterCelebration = true
                                Toast.makeText(context, "💧 Pohon berhasil disiram! Tumbuh semakin subur!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "🌸 Pohon sudah segar hari ini! Datang lagi besok ya!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shower, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isWateredToday) "Sudah Disiram Hari Ini 🌸" else "Siram Pohon 💧 (+$waterRewardPoints Poin)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Button to Album Stiker
            TactileBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                shadowColor = LituturSurfaceVariant,
                cornerRadius = 16.dp,
                onClick = onNavigateToAlbum
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LituturPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Koleksi Stiker Lengkap 🌟", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                }
            }
        }
    }
}

/** Satu buah di pohon: stiker yang sudah didapat, atau kuncup bunga bila belum ada stikernya. */
@Composable
private fun StickerFruit(
    sticker: StikerModel?,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (sticker != null) Color.White else Color.White.copy(alpha = 0.55f))
            .border(
                width = 2.dp,
                color = if (sticker != null) LituturPrimaryContainer else Color.White.copy(alpha = 0.9f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            sticker == null -> Text("🌸", fontSize = 16.sp)
            sticker.imageUrl.isBlank() -> Icon(
                Icons.Default.Pets,
                contentDescription = sticker.name,
                tint = LituturPrimaryContainer,
                modifier = Modifier.size(size * 0.6f)
            )
            else -> AsyncImage(
                model = sticker.imageUrl,
                contentDescription = sticker.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 11.sp, color = LituturOutline)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor, textAlign = TextAlign.Center)
        }
    }
}
