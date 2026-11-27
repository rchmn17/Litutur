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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*

private data class PohonMilestone(
    val level: Int,
    val name: String,
    val emoji: String,
    val minStories: Int,
    val description: String,
    val imageUrl: String
)

private data class InteractiveFruit(
    val id: Int,
    val name: String,
    val iconEmoji: String,
    val requiredStories: Int,
    val horizontalBias: Float,
    val verticalBias: Float
)

@Composable
fun PohonKisahScreen(
    childName: String = "Budi",
    childPoints: Int = 135,
    storiesCount: Int = 0,
    onRequestParentMode: () -> Unit = {},
    onNavigateToAlbum: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    val milestones = remember {
        listOf(
            PohonMilestone(
                level = 1,
                name = "Tunas Ceria",
                emoji = "🌱",
                minStories = 0,
                description = "Tunas mungil penuh harapan mulai bertumbuh berkat awal petualangan membacamu!",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAYiGzFhozN3--zZ-Nx7pGkMUBPQ8JtkE_xWv8BkeiMWXwtoZrESEQYxJY4UCjGjypxSZUguZjKBgNqPxLwFxK5t-RaP_MYxz1J82QHSyryRaiiO0_tyd6xJY403ECiaOXoqOINE1s3chVCMOvzS30j8GN7vzUF3TvsuDXH1W2DWvdgOaYaOtxojHFRVfahe0F4gOLITGiQdINQm_DqwMI837KWhd2F6kpujnmsyf1E"
            ),
            PohonMilestone(
                level = 2,
                name = "Pohon Muda Rimbun",
                emoji = "🌿",
                minStories = 2,
                description = "Batang pohon semakin kokoh dan daun-daun hijau yang rimbun mulai menyejukkan!",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAYiGzFhozN3--zZ-Nx7pGkMUBPQ8JtkE_xWv8BkeiMWXwtoZrESEQYxJY4UCjGjypxSZUguZjKBgNqPxLwFxK5t-RaP_MYxz1J82QHSyryRaiiO0_tyd6xJY403ECiaOXoqOINE1s3chVCMOvzS30j8GN7vzUF3TvsuDXH1W2DWvdgOaYaOtxojHFRVfahe0F4gOLITGiQdINQm_DqwMI837KWhd2F6kpujnmsyf1E"
            ),
            PohonMilestone(
                level = 3,
                name = "Pohon Berbuah Manis",
                emoji = "🌳🍎",
                minStories = 4,
                description = "Pohonmu telah lebat dan memunculkan buah-buah kisah manis penuh hikmah!",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAYiGzFhozN3--zZ-Nx7pGkMUBPQ8JtkE_xWv8BkeiMWXwtoZrESEQYxJY4UCjGjypxSZUguZjKBgNqPxLwFxK5t-RaP_MYxz1J82QHSyryRaiiO0_tyd6xJY403ECiaOXoqOINE1s3chVCMOvzS30j8GN7vzUF3TvsuDXH1W2DWvdgOaYaOtxojHFRVfahe0F4gOLITGiQdINQm_DqwMI837KWhd2F6kpujnmsyf1E"
            ),
            PohonMilestone(
                level = 4,
                name = "Pohon Emas Ajaib",
                emoji = "✨🌟",
                minStories = 7,
                description = "Mahakarya luar biasa! Pohon bertransformasi menjadi Pohon Emas Legendaris!",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAYiGzFhozN3--zZ-Nx7pGkMUBPQ8JtkE_xWv8BkeiMWXwtoZrESEQYxJY4UCjGjypxSZUguZjKBgNqPxLwFxK5t-RaP_MYxz1J82QHSyryRaiiO0_tyd6xJY403ECiaOXoqOINE1s3chVCMOvzS30j8GN7vzUF3TvsuDXH1W2DWvdgOaYaOtxojHFRVfahe0F4gOLITGiQdINQm_DqwMI837KWhd2F6kpujnmsyf1E"
            )
        )
    }

    val currentMilestoneIndex = when {
        storiesCount >= 7 -> 3
        storiesCount >= 4 -> 2
        storiesCount >= 2 -> 1
        else -> 0
    }
    val currentMilestone = milestones[currentMilestoneIndex]

    val nextMilestoneStories = if (currentMilestoneIndex < milestones.size - 1) {
        (milestones[currentMilestoneIndex + 1].minStories - storiesCount).coerceAtLeast(1)
    } else {
        0
    }

    val baseProgress = remember(storiesCount, childPoints) {
        val storyRatio = (storiesCount.toFloat() / 7f).coerceIn(0.15f, 0.95f)
        val pointsBonus = (childPoints / 500f).coerceIn(0f, 0.25f)
        (storyRatio * 0.75f + pointsBonus).coerceIn(0.15f, 1f)
    }
    var treeProgress by remember(baseProgress) { mutableFloatStateOf(baseProgress) }
    var isWateredToday by remember { mutableStateOf(false) }
    var showWaterCelebration by remember { mutableStateOf(false) }

    val interactiveFruits = remember {
        listOf(
            InteractiveFruit(1, "Buah Keberanian", "🍎", 1, -0.6f, -0.2f),
            InteractiveFruit(2, "Buah Kebaikan", "🍎", 2, 0.55f, -0.3f),
            InteractiveFruit(3, "Buah Kejujuran", "🍎", 3, -0.3f, -0.55f),
            InteractiveFruit(4, "Buah Kecerdikan", "🍎", 4, 0.4f, 0.1f),
            InteractiveFruit(5, "Buah Emas Kebijaksanaan", "🌟", 7, 0.05f, -0.45f)
        )
    }
    val unlockedFruitsCount = interactiveFruits.count { storiesCount >= it.requiredStories }

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
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LituturPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Face, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text("LITUTUR ANAK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LituturOutline)
                            Text(
                                "Halo, $childName! 🌟",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LituturTextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LituturSurfaceHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = LituturSecondary, modifier = Modifier.size(18.dp))
                                Text(
                                    "$childPoints Koin",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LituturTextDark,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        IconButton(
                            onClick = onRequestParentMode,
                            modifier = Modifier
                                .size(38.dp)
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
                    selected = false,
                    onClick = { onBottomTabSelected(0) },
                    icon = { Icon(Icons.Default.AutoStories, null) },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Park, null) },
                    label = { Text("Pohon Kisah", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = LituturPurple)
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onBottomTabSelected(2) },
                    icon = { Icon(Icons.Default.Stars, null) },
                    label = { Text("Album") }
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
                        .background(Color.Transparent)
                        .clickable { onNavigateToAlbum() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐ Album Stiker", fontSize = 13.sp, fontWeight = FontWeight.Normal, color = LituturTextVariant)
                }
            }

            // Tree Visual Canvas Box with Interactive Fruits
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(LituturSurfaceHigh)
            ) {
                AsyncImage(
                    model = currentMilestone.imageUrl,
                    contentDescription = "Pohon Kisah",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

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
                            "${currentMilestone.emoji} Tahap ${currentMilestone.level}/4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LituturSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Interactive Fruit Hotspots positioned on tree branches
                interactiveFruits.forEach { fruit ->
                    val isUnlocked = storiesCount >= fruit.requiredStories
                    Box(
                        modifier = Modifier
                            .align(BiasAlignment(fruit.horizontalBias, fruit.verticalBias))
                            .clip(RoundedCornerShape(50))
                            .background(if (isUnlocked) Color.White.copy(alpha = 0.92f) else Color.Black.copy(alpha = 0.45f))
                            .clickable {
                                if (isUnlocked) {
                                    Toast.makeText(
                                        context,
                                        "${fruit.iconEmoji} ${fruit.name} mekar! Hadiah membaca ${fruit.requiredStories} dongeng 🌟",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "🔒 Baca ${fruit.requiredStories} dongeng untuk memetik ${fruit.name}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(if (isUnlocked) fruit.iconEmoji else "🔒", fontSize = 13.sp)
                            Text(
                                if (isUnlocked) "Kisah ${fruit.requiredStories}" else "${fruit.requiredStories} Kisah",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) LituturTextDark else Color.White
                            )
                        }
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
                        "${currentMilestone.emoji} ${currentMilestone.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LituturPurple,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Milestone Stepper Card (Tahapan Pertumbuhan Pohon)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = LituturPrimary)
                            Text("Tahapan Pertumbuhan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        }
                        Text(
                            "Tahap ${currentMilestone.level} dari 4",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LituturPurple
                        )
                    }

                    // Stepper Icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        milestones.forEachIndexed { index, ms ->
                            val isReached = currentMilestoneIndex >= index
                            val isCurrent = currentMilestoneIndex == index

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCurrent -> LituturPurple
                                                isReached -> LituturSecondaryContainer
                                                else -> LituturSurfaceHigh
                                            }
                                        )
                                        .then(
                                            if (isCurrent) Modifier.border(2.dp, LituturPurpleDark, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(ms.emoji, fontSize = 15.sp)
                                }
                                Text(
                                    ms.name.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) LituturPurple else LituturTextVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "${ms.minStories}+ Kisah",
                                    fontSize = 9.sp,
                                    color = LituturOutline,
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (index < milestones.size - 1) {
                                Divider(
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .padding(bottom = 20.dp),
                                    color = if (currentMilestoneIndex > index) LituturPurple else LituturSurfaceVariant,
                                    thickness = 2.dp
                                )
                            }
                        }
                    }

                    // Stage Description callout
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LituturSurfaceHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                currentMilestone.description,
                                fontSize = 12.sp,
                                color = LituturTextDark,
                                lineHeight = 17.sp
                            )
                            if (nextMilestoneStories > 0) {
                                Text(
                                    "✨ Baca $nextMilestoneStories dongeng lagi untuk berevolusi ke tahap berikutnya!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LituturSecondary
                                )
                            } else {
                                Text(
                                    "🌟 Hebat! Pohonmu telah mekar sempurna sebagai Pohon Emas!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LituturPurple
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("📖 Dongeng", fontSize = 11.sp, color = LituturOutline)
                        Text("$storiesCount Selesai", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("🍎 Buah Kisah", fontSize = 11.sp, color = LituturOutline)
                        Text("$unlockedFruitsCount / ${interactiveFruits.size} Mekar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturSecondary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("💧 Kondisi", fontSize = 11.sp, color = LituturOutline)
                        Text(if (isWateredToday) "Sangat Segar" else "Perlu Air", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isWateredToday) LituturPrimary else LituturTertiary)
                    }
                }
            }

            // Watering Card ("Siram Pohon")
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${(treeProgress * 100).toInt()}% Menuju Tingkat Sempurna", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                            Text(
                                if (nextMilestoneStories > 0) "$nextMilestoneStories Cerita Lagi" else "Maksimal!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LituturSecondary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { treeProgress },
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
                                treeProgress = (treeProgress + 0.05f).coerceAtMost(1f)
                                isWateredToday = true
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
                                if (isWateredToday) "Pohon Sudah Segar Hari Ini 🌸" else "Siram Pohon (+5% Pertumbuhan) 💧",
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
