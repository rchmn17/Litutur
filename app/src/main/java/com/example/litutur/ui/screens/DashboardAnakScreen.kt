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
import com.example.litutur.ui.components.TactileBox
import com.example.litutur.ui.theme.*

data class CeritaBuku(
    val title: String,
    val imageUrl: String,
    val badgeText: String?,
    val badgeColor: Color,
    val level: String,
    val rating: String,
    val duration: String
)

@Composable
fun DashboardAnakScreen(
    onNavigateToRead: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(0) }
    val categories = listOf("Semua Cerita", "🦁 Fabel", "🚀 Petualangan", "🧚 Dongeng", "🔬 Sains")

    val bookList = remember {
        listOf(
            CeritaBuku(
                title = "Singa yang Baik Hati",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAMnMXGf4IqB7Jm1Mq5egbAeEzKw_3XEnU30CfUV6WJFXOJegHqG1pCBDvzUrsQviuBFBXsVOtzjUpLtdleUkJQJSC6W4Yk570CAyj2iMiGbehUIkvINUVJk0buCcILXZ_fzTna8ujwJ0D0j_hjbVrMoq5mf7gopBNI3GhsnW9e_1BvxCGZmRCGn4HybVWI4MlaYGIyI9KmZ1vvOTA9kZHYQLOUK3GRNj2HdAaxDYoY",
                badgeText = "Favorit",
                badgeColor = LituturPrimaryContainer,
                level = "Level 2",
                rating = "4.9",
                duration = "8 mnt"
            ),
            CeritaBuku(
                title = "Kelinci & Kura-kura",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAStevGosNmGTg0TQxsaYPJXmARrMwDYBIrrB_f94250v5rjLoEcd0DXH31qRrBIv6g3k1ER9ZuJ9BQW_LfnwTNLAG-Soau0YHDjb-wywx8u35cylLK5NfWuhXP6UoCgRhDU7eWrHKQ-0Mr0xs1F2AvEMVaVC-oKbjcI0skimC-gIcJvNheshm8exfB22aCyrCxR3TTScmZRcD7pcYhkKqDucX67vRGwEH563uK-qOQ",
                badgeText = "Baru",
                badgeColor = LituturSecondaryContainer,
                level = "Level 1",
                rating = "4.8",
                duration = "5 mnt"
            ),
            CeritaBuku(
                title = "Misteri Bintang Kejora",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCG-kdAJnekprDeOPX75fLVJ87g-yL76mMQQiqPzifUrSSm2RWFM8Q2PBL_-UfQ9Kfx5iZzu_JhCfY9Xpov-CQNKWPVCUItvOTqh_UUjdvWvaQA_eOMX1Xgi6_tM9qCu5Hun3jrlTUa10bI_QN9K1w_enTh87lDVNA62VMDqzLYM72SRm8LN43HdaHa-1WO0YfA5RYQsml6ZnPSL6N_hSoGU-3IuhtIeckyCgcPQIc1",
                badgeText = null,
                badgeColor = Color.Transparent,
                level = "Level 2",
                rating = "5.0",
                duration = "10 mnt"
            ),
            CeritaBuku(
                title = "Petualangan Si Kancil",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCzVO_A2s58Q39eTvzKo-EIiCxyW0FTpp2R7COJUQMfo1t5hvgNtvq1ubSitCjq4tP7xbVy3Oi-cbq0B9xK8Y48YSbBbXf23FnDbl5fIQB0588aA6HsKxQjUKCRLi99KM0qAIuAK-9tfNYCW5iUMTLsX8qyNEwMNLWiD8NPkoT8zbDmuGRkfJeb83fI_n_Im5faCOW13KzdWMTcYM3buhBqefvAGWRa05-rLlG8alJq",
                badgeText = null,
                badgeColor = Color.Transparent,
                level = "Level 3",
                rating = "4.9",
                duration = "7 mnt"
            )
        )
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
                            Text("Halo, Budi! 🌟", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
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
                            Text("120 Poin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        }
                        IconButton(
                            onClick = {},
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
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlZm93mKuue9mdTbnk8VDHYRFeeAWVUDUd9aKstzHy1Q7yIa5bCaETgeRTbyT28amYwl-_U8I4Rwrzp8QFpNwAQiDWQ79HVjj2KaVj3lnBLd74r50Rq41Gi5YD4CS7aGgJyGT3azocFD8WTJY2u63rhWldFqjMNiSbuesHAUDvjEApFe-YEoy2qJJCCykVWF1wKW1mCxWNMVeZsmiqus4k04PqeZZTVCc-210SdBMY",
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
                            Text("Level 3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturSecondary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Pohon Kisahku 🌳", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text("Siap Disiram! 💧", fontSize = 12.sp, color = LituturTextVariant)
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
                            Text("⭐ 8/20", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturTertiary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Album Stiker ⭐", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LituturTextDark)
                        Text("Terkumpul 8", fontSize = 12.sp, color = LituturTextVariant)
                    }
                }
            }

            TactileBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                shadowColor = LituturSurfaceVariant
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
                                Text("Baca 1 cerita lagi yuk!", fontSize = 12.sp, color = LituturTextVariant)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LituturSecondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LituturSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+20 Poin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturSecondary)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { 0.5f },
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
                    val isSelected = selectedCategory == index
                    TactileBox(
                        backgroundColor = if (isSelected) LituturPurple else Color.White,
                        shadowColor = if (isSelected) LituturPurpleDark else LituturSurfaceVariant,
                        cornerRadius = 50.dp,
                        shadowDepth = 3.dp,
                        onClick = { selectedCategory = index }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else LituturTextVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                for (chunk in bookList.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (book in chunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                BookCardItem(book = book, onReadClick = { onNavigateToRead(book.title) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookCardItem(book: CeritaBuku, onReadClick: () -> Unit) {
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
                    model = book.imageUrl,
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (book.badgeText != null) {
                    Text(
                        text = book.badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(50))
                            .background(book.badgeColor)
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
                    Text(book.duration, fontSize = 11.sp, color = LituturTextVariant)
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
