package com.example.litutur.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.data.model.StikerModel
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*

@Composable
fun AlbumStikerScreen(
    childName: String = "Budi",
    childPoints: Int = 135,
    storiesCount: Int = 0,
    stickers: List<StikerModel> = emptyList(),
    coins: Int = childPoints,
    mysteryBoxCost: Int = 25,
    revealedSticker: StikerModel? = null,
    onOpenMysteryBox: () -> Unit = {},
    onDismissReveal: () -> Unit = {},
    onRequestParentMode: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFilterIndex by remember { mutableStateOf(0) }

    // Filter dibangun dari kategori yang benar-benar ada di data stiker
    val categoryNames = remember(stickers) { stickers.map { it.category }.distinct() }
    val filters = listOf("Semua (${stickers.size})") + categoryNames
    val safeFilterIndex = selectedFilterIndex.coerceIn(0, filters.lastIndex)

    val visibleStickers = if (safeFilterIndex == 0) {
        stickers
    } else {
        stickers.filter { it.category == filters[safeFilterIndex] }
    }

    val unlockedCount = stickers.count { it.isUnlocked }
    val hasLockedSticker = stickers.any { !it.isUnlocked }
    val canOpenBox = hasLockedSticker && coins >= mysteryBoxCost

    if (revealedSticker != null) {
        MysteryBoxRevealDialog(sticker = revealedSticker, onDismiss = onDismissReveal)
    }
    val unlockedProgress = unlockedCount.toFloat() / stickers.size.coerceAtLeast(1).toFloat()

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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            "Koleksi Stiker $childName ⭐",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LituturTextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Selesaikan bacaan & kuis untuk membuka stiker!",
                            fontSize = 11.sp,
                            color = LituturOutline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = LituturSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "$coins Koin",
                                    fontSize = 12.sp,
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
                    selected = false,
                    onClick = { onBottomTabSelected(1) },
                    icon = { Icon(Icons.Default.Park, null) },
                    label = { Text("Pohon Kisah") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Stars, null) },
                    label = { Text("Album", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = LituturPurple)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(LituturBgStart, LituturBgMid, LituturBgEnd)))
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progres Album Seri 1", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text("$unlockedCount / ${stickers.size} Terkumpul (${(unlockedProgress * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturSecondary)
                    }
                    LinearProgressIndicator(
                        progress = { unlockedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = LituturSecondaryDim,
                        trackColor = LituturSurfaceHigh
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEachIndexed { idx, filter ->
                    val isSelected = safeFilterIndex == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) LituturPrimaryContainer else Color.White)
                            .clickable { selectedFilterIndex = idx }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else LituturTextVariant
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleStickers, key = { it.id }) { sticker ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LituturSurfaceHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!sticker.isUnlocked) {
                                    Icon(Icons.Default.Lock, contentDescription = "Terkunci", tint = LituturOutline)
                                } else if (sticker.imageUrl.isBlank()) {
                                    Icon(Icons.Default.Pets, contentDescription = sticker.name, tint = LituturPrimaryContainer, modifier = Modifier.size(32.dp))
                                } else {
                                    AsyncImage(
                                        model = sticker.imageUrl,
                                        contentDescription = sticker.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Text(
                                sticker.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!sticker.isUnlocked) LituturOutline else LituturTextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (sticker.isUnlocked) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, sticker.soundText, Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LituturSurfaceHigh),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.fillMaxWidth().height(26.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = LituturTextDark, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Suara", fontSize = 10.sp, color = LituturTextDark, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(sticker.levelText, fontSize = 9.sp, color = LituturOutline, maxLines = 1)
                            }
                        }
                    }
                }
            }

            TactileBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (canOpenBox) LituturPrimary else LituturOutline,
                shadowColor = if (canOpenBox) LituturPrimaryShadow else LituturSurfaceVariant,
                cornerRadius = 14.dp,
                // Tetap bisa ditekan saat koin kurang agar anak mendapat penjelasan (Toast)
                onClick = onOpenMysteryBox
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Redeem, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        when {
                            !hasLockedSticker -> "Semua Stiker Terkumpul! 🏆"
                            canOpenBox -> "Buka Kotak Stiker Misterius (-$mysteryBoxCost Koin)"
                            else -> "Kotak Misterius: butuh ${mysteryBoxCost - coins} koin lagi"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MysteryBoxRevealDialog(sticker: StikerModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Hore! Simpan ke Album", fontWeight = FontWeight.Bold, color = LituturPrimary)
            }
        },
        title = {
            Text("Kotak Misterius Terbuka! ✨", fontWeight = FontWeight.Bold, color = LituturTextDark)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(LituturSurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (sticker.imageUrl.isBlank()) {
                        Icon(Icons.Default.Pets, contentDescription = sticker.name, tint = LituturPrimaryContainer, modifier = Modifier.size(56.dp))
                    } else {
                        AsyncImage(
                            model = sticker.imageUrl,
                            contentDescription = sticker.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(sticker.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                Text(sticker.category, fontSize = 12.sp, color = LituturTextVariant)
                Text(sticker.soundText, fontSize = 13.sp, color = LituturTextDark)
            }
        }
    )
}
