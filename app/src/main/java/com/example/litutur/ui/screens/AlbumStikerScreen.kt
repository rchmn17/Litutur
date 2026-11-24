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
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*

data class StikerHewan(
    val name: String,
    val imageUrl: String,
    val soundText: String,
    val isLocked: Boolean,
    val levelText: String
)

@Composable
fun AlbumStikerScreen(
    childName: String = "Budi",
    childPoints: Int = 135,
    storiesCount: Int = 0,
    onRequestParentMode: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf("Semua (6)", "Sahabat Rimba", "Petualang Langit", "Legendaris")

    val stickers = remember(storiesCount) {
        listOf(
            StikerHewan("Leo Singa", "https://lh3.googleusercontent.com/aida-public/AB6AXuBJ5mHf8G0M15Voefy2MTmlGvGwhsjWmIgxFlACRwLiwvxJ98JNcpSTx1N5IeYqpOqhyzxyz9IEGkL1pVh_kiagmnxsuFSw2U1HihSruMoYTmjrucLyDHvcJnICmyytzF-acdjZ5Nu6K7AGo6uJMg8ZQnuE7YskaKQpPK8xhtgN-NJXmpW_0j0HacirOb6M6uQoiA4iUoju05I28Q3ZbbcbYoz9s3nUrwDtvgZGungc", "Leo Singa: Rawrr ceria! 🦁", false, "Lvl 1"),
            StikerHewan("Kelinci Ceria", "https://lh3.googleusercontent.com/aida-public/AB6AXuDUUMx2TSYoKhZwSIEHjQb3jv6L4dw4M3kYHcEnxIk72iZ6i0shtbjj-l98JTyOByZ-mlynbhF7ym_yUgAHnE5o9jwFDRz-_uJcJxGZ-ZjlGF7ZIa8iQ6mmSItRdlA991UCvGlkL9VTewFUsKaICjVecKfVGBGGjd_xBcUxIIlygewk7fZMnPqcufYacBXkVtL5-iy1kuDGGlODE4DvLpV19flaVdozwOuOzSsGDdVM", "Kelinci Ceria: Hop hop senang! 🐰", storiesCount < 1, "Baca 1 Cerita"),
            StikerHewan("Kancil Cerdik", "https://lh3.googleusercontent.com/aida-public/AB6AXuDUkxy0eP7Y7yN0DLskfzvocBBrwKgdZKEDhX1eTPFLoNXV7dhTc1OeEwZ0Rzx2QVaHkB-R2si5a3j2oMsoZ85lBBeX46_Eaw5qi4OU4XtyfiT_s7GwW8ZXzcNh_moIW78Icnarhkxo8VfWLrrYXqQZQ7KFmHqVJo4UGYMVv2KgRmkpNbuMWgkC6pOpDQ4PdEAWUUbYCxnO6fiQKcpDhdL3vmr6S0-qcb5gyda-eglH", "Kancil Cerdik: Siap berpetualang!", storiesCount < 2, "Baca 2 Cerita"),
            StikerHewan("Burung Hantu", "https://lh3.googleusercontent.com/aida-public/AB6AXuAEBr3z4JawPmT-irh9R3xBCU3BRHCfzTOuhKl_U5tjm2w_korbCcEGuVhc_iXU23ufygJkj-6kVFxv99IYxvdWleio0dZugoA4k3rI73WLZNcelc-42YTOKzfHDxGfa7-gRngQUulkXM3-Vs8pn1r6zdPRdItIO8mrcj2O3H4hGhM4PHluO7ZJbRFTisCz1864cmgDQnlk0ETaehMzBub6euoGtJZlwhChxvG3-z_C", "Hantu Bijak: Hoot hoot!", storiesCount < 3, "Baca 3 Cerita"),
            StikerHewan("Gajah Ramah", "https://lh3.googleusercontent.com/aida-public/AB6AXuCB5s0CW6951d4itUO9q0Y-aYXB2PZ8yivlJvhDHwGjKWsM_m5Rx_f56NOBuyNDzI6O6WME5gxmwgDBGsxCLxiJkw6EQAAWpaFvVqUcLa9gnPl6Nc6Wm6uNWuyGv-5gBFlHxA6HsAH3jOlCl_-JMYYEBQh5KMFGUimsA_CPPcSqFbW3pztA2U9C6I_gzDf-acUj5LQpaO4ue3IMpINwb0XOG6rrI8DhAg2OAQHA5J58", "Gajah: Teeeet semburan air! 🐘", storiesCount < 4, "Baca 4 Cerita"),
            StikerHewan("Harimau Rimba", "", "", storiesCount < 5, "Baca 5 Cerita")
        )
    }
    val unlockedCount = stickers.count { !it.isLocked }
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
                    Column {
                        Text("Koleksi Stiker $childName ⭐", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text("Selesaikan bacaan & kuis untuk membuka stiker!", fontSize = 11.sp, color = LituturOutline)
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
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LituturSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$childPoints Koin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                NavigationBarItem(selected = false, onClick = { onBottomTabSelected(1) }, icon = { Icon(Icons.Default.Park, null) }, label = { Text("Pohon Kisah") })
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Stars, null) },
                    label = { Text("Album", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = LituturPurple)
                )
                NavigationBarItem(selected = false, onClick = { onBottomTabSelected(3) }, icon = { Icon(Icons.Default.MilitaryTech, null) }, label = { Text("Prestasi") })
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
                    val isSelected = selectedFilterIndex == idx
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
                items(stickers) { sticker ->
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
                                if (sticker.isLocked) {
                                    Icon(Icons.Default.Lock, contentDescription = "Terkunci", tint = LituturOutline)
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
                                color = if (sticker.isLocked) LituturOutline else LituturTextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!sticker.isLocked) {
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
                backgroundColor = LituturPrimary,
                shadowColor = LituturPrimaryShadow,
                cornerRadius = 14.dp,
                onClick = {
                    Toast.makeText(context, "Membuka Kotak Stiker Misterius... ✨", Toast.LENGTH_SHORT).show()
                }
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
                    Text("Buka Kotak Stiker Misterius (-25 Koin)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
