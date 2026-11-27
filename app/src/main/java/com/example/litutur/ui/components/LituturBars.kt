package com.example.litutur.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.litutur.ui.theme.LituturOutline
import com.example.litutur.ui.theme.LituturPrimaryContainer
import com.example.litutur.ui.theme.LituturPurple
import com.example.litutur.ui.theme.LituturSecondary
import com.example.litutur.ui.theme.LituturSurface
import com.example.litutur.ui.theme.LituturSurfaceHigh
import com.example.litutur.ui.theme.LituturSurfaceVariant
import com.example.litutur.ui.theme.LituturTextDark
import com.example.litutur.ui.theme.LituturTextVariant
import com.example.litutur.ui.theme.ScreenBaseCanvas
import com.example.litutur.ui.theme.TextPrimarySlate
import java.util.Locale

// =====================================================================
// MODE ORANG TUA
// =====================================================================

/** Header seragam untuk semua tab Mode Orang Tua. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentTopBar(
    title: String,
    subtitle: String = "LITUTUR PARENT",
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6BFE9C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = Color(0xFF00210C),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006D37)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimarySlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBaseCanvas)
    )
}

// =====================================================================
// MODE ANAK
// =====================================================================

/** Tab bawah Mode Anak. Index dipakai oleh navigasi (0 = Beranda, 1 = Pohon, 2 = Album). */
enum class ChildTab(val label: String, val icon: ImageVector) {
    HOME("Beranda", Icons.Default.AutoStories),
    TREE("Pohon Kisah", Icons.Default.Park),
    ALBUM("Album", Icons.Default.Stars)
}

/** Navigasi bawah seragam untuk Beranda, Pohon Kisah, dan Album Stiker. */
@Composable
fun ChildBottomBar(
    selected: ChildTab,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        ChildTab.entries.forEach { tab ->
            val isSelected = tab == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { if (!isSelected) onTabSelected(tab.ordinal) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        tab.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = LituturPurple,
                    indicatorColor = LituturPurple,
                    unselectedIconColor = LituturOutline,
                    unselectedTextColor = LituturOutline
                )
            )
        }
    }
}

/**
 * Header utama Mode Anak: avatar, sapaan, saldo koin, dan tombol gembok Parental Gate.
 */
@Composable
fun ChildHomeTopBar(
    title: String,
    coins: Int,
    onRequestParentMode: () -> Unit,
    label: String = "LITUTUR ANAK",
    icon: ImageVector = Icons.Filled.Face
) {
    Surface(color = LituturSurface.copy(alpha = 0.97f), shadowElevation = 2.dp) {
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LituturPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LituturOutline)
                    Text(
                        title,
                        fontSize = 17.sp,
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
                CoinBadge(coins = coins)
                IconButton(
                    onClick = onRequestParentMode,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LituturSurfaceVariant)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = "Masuk Mode Orang Tua", tint = LituturTextVariant)
                }
            }
        }
    }
}

/** Badge saldo koin yang sama di semua layar anak. */
@Composable
fun CoinBadge(coins: Int) {
    Surface(shape = RoundedCornerShape(50), color = LituturSurfaceHigh) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Filled.MonetizationOn,
                contentDescription = null,
                tint = LituturSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "$coins Koin",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LituturTextDark,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/** Header layar turunan Mode Anak (Detail Buku, Baca Cerita, Kuis). */
@Composable
fun ChildDetailTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = LituturSurface.copy(alpha = 0.97f), shadowElevation = 2.dp) {
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
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChildIconAction(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    onClick = onBack,
                    containerColor = LituturSurfaceHigh,
                    tint = LituturTextDark
                )
                Text(
                    title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = LituturTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
    }
}

/** Tombol ikon bulat-persegi standar di header Mode Anak. */
@Composable
fun ChildIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color = LituturSurfaceVariant,
    tint: Color = LituturTextDark
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint)
    }
}

// =====================================================================
// TEXT-TO-SPEECH bersama (narasi buku, suara soal kuis, suara stiker)
// =====================================================================

@Stable
class LituturSpeaker internal constructor() {
    internal var engine: TextToSpeech? = null
    var isReady by mutableStateOf(false)
        internal set

    /** Membacakan teks. Mengembalikan false jika mesin suara belum siap. */
    fun speak(text: String): Boolean {
        val tts = engine
        if (!isReady || tts == null || text.isBlank()) return false
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "litutur_${System.currentTimeMillis()}")
        return true
    }

    fun stop() {
        engine?.stop()
    }
}

@Composable
fun rememberLituturSpeaker(pitch: Float = 1.15f, speechRate: Float = 0.85f): LituturSpeaker {
    val context = LocalContext.current
    val speaker = remember { LituturSpeaker() }
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("id", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
                tts?.setPitch(pitch)
                tts?.setSpeechRate(speechRate)
                speaker.isReady = true
            }
        }
        speaker.engine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
            speaker.engine = null
            speaker.isReady = false
        }
    }
    return speaker
}
