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
import com.example.litutur.data.repository.TreeGrowthData
import com.example.litutur.data.remote.LituturAssets
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.components.rememberAssetUrl
import com.example.litutur.ui.theme.*

@Composable
fun PohonKisahScreen(
    childName: String = "Budi",
    childPoints: Int = 135,
    storiesCount: Int = 0,
    treeGrowth: TreeGrowthData = TreeGrowthData(1, "Tunas Kecil 🌱", 0.5f, 25, "Bibit Hijau"),
    isWateredToday: Boolean = false,
    waterRewardPoints: Int = 5,
    onWaterTree: () -> Unit = {},
    onRequestParentMode: () -> Unit = {},
    onNavigateToAlbum: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val isMaxStage = treeGrowth.pointsToNext <= 0

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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Text("Halo, $childName! 🌟", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = LituturSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$childPoints Koin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                NavigationBarItem(selected = false, onClick = { onBottomTabSelected(0) }, icon = { Icon(Icons.Default.AutoStories, null) }, label = { Text("Beranda") })
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Park, null) },
                    label = { Text("Pohon Kisah", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = LituturPurple)
                )
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LituturSurfaceHigh)
            ) {
                AsyncImage(
                    model = rememberAssetUrl(LituturAssets.POHON_KISAH.sourceUrl),
                    contentDescription = "Pohon Kisah",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "🌱 Tumbuh berkat $storiesCount dongeng!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LituturPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Text(
                        "Tahap ${treeGrowth.stage} • ${treeGrowth.stageLabel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LituturSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(LituturSecondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = LituturTertiary)
                        Text("Siram Pohon dengan Membaca!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (isMaxStage) "Pohon sudah tumbuh maksimal!" else "${(treeGrowth.progress * 100).toInt()}% Menuju ${treeGrowth.nextStageLabel}",
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

                    TactileBox(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isWateredToday) LituturSecondary else LituturPurple,
                        shadowColor = if (isWateredToday) LituturSecondaryDim else LituturPurpleDark,
                        cornerRadius = 14.dp,
                        onClick = onWaterTree
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
