package com.example.litutur.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.LituturTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentSettingsScreen(
    onLogout: () -> Unit = {}
) {
    var isWhatsAppDigestEnabled by remember { mutableStateOf(true) }
    var showPinDialog by remember { mutableStateOf(false) }

    if (showPinDialog) {
        ParentPinDialog(
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
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
                                text = "LITUTUR PARENT",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006D37)
                            )
                            Text(
                                text = "Parent Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CoralOrangePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil Bunda",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F9FD)
                )
            )
        },
        containerColor = Color(0xFFF8F9FD)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. Top Action & Context Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF6BFE9C).copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF005228),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MODE AMAN ORANG TUA",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF005228)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCE9FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CoralOrangePrimary)
                        )
                    }
                }
            }

            // 2. Parent Profile Header Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar with Shield Badge
                            Box(modifier = Modifier.size(64.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFDAD2))
                                        .border(2.dp, CoralOrangePrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar Bunda",
                                        tint = CoralOrangePrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00D084)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Bunda Sari",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "bunda.sari@litutur.id",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(Color(0xFFE6EEFF))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = CoralOrangePrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Akun Terverifikasi ✨",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF1E293B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF4FF))
                                .clickable { }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = CoralOrangePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Profil & Keluarga",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralOrangePrimary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Group 1: Kelola Profil & Akses
            item {
                SettingsSectionGroup(
                    title = "KELOLA PROFIL & AKSES",
                    items = listOf(
                        SettingsRowData(
                            icon = Icons.Default.Face,
                            iconBg = Color(0xFFCCE5FF),
                            iconTint = Color(0xFF006397),
                            title = "Profil Anak",
                            subtitle = "Kayla (6 thn) & Bimo (4 thn)",
                            badgeText = "2 Profil",
                            badgeColor = Color(0xFF006397),
                            badgeBg = Color(0xFFE6EEFF),
                            onClick = { }
                        ),
                        SettingsRowData(
                            icon = Icons.Default.Lock,
                            iconBg = Color(0xFF6BFE9C),
                            iconTint = Color(0xFF00210C),
                            title = "Kunci Pengaman Orang Tua",
                            subtitle = "Parental Gate PIN 4 Digit",
                            badgeText = "Aktif (••••)",
                            badgeColor = Color(0xFF006D37),
                            badgeBg = Color(0xFFF0FDF4),
                            onClick = { showPinDialog = true }
                        ),
                        SettingsRowData(
                            icon = Icons.Default.HourglassBottom,
                            iconBg = Color(0xFFFFDAD2),
                            iconTint = CoralOrangePrimary,
                            title = "Batas Layar Anak",
                            subtitle = "Timer jeda istirahat mata",
                            badgeText = "30 Mnt/Hari",
                            badgeColor = CoralOrangePrimary,
                            badgeBg = Color(0xFFFFF4F0),
                            onClick = { }
                        )
                    )
                )
            }

            // 4. Group 2: Preferensi & Privasi
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PREFERENSI & PRIVASI",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // WA Toggle Item
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF4AE183).copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = null,
                                            tint = Color(0xFF006D37),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "Laporan Mingguan WA",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Rangkuman literasi dikirim tiap Senin",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isWhatsAppDigestEnabled,
                                    onCheckedChange = { isWhatsAppDigestEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF00D084)
                                    )
                                )
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                            SettingsRow(
                                data = SettingsRowData(
                                    icon = Icons.Default.PrivacyTip,
                                    iconBg = Color(0xFFCCE5FF),
                                    iconTint = Color(0xFF006397),
                                    title = "Privasi Komunitas",
                                    subtitle = "Pengaturan foto karya baca",
                                    badgeText = "Nama Samaran",
                                    badgeColor = Color(0xFF64748B),
                                    badgeBg = Color(0xFFE6EEFF),
                                    onClick = { }
                                )
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                            SettingsRow(
                                data = SettingsRowData(
                                    icon = Icons.Default.Language,
                                    iconBg = Color(0xFFDCE9FF),
                                    iconTint = Color(0xFF1E293B),
                                    title = "Bahasa Narasi",
                                    subtitle = "Teks & sulih suara cerita",
                                    badgeText = "Bahasa ID",
                                    badgeColor = Color(0xFF64748B),
                                    badgeBg = Color(0xFFE6EEFF),
                                    onClick = { }
                                )
                            )
                        }
                    }
                }
            }

            // 5. Group 3: Dukungan
            item {
                SettingsSectionGroup(
                    title = "DUKUNGAN",
                    items = listOf(
                        SettingsRowData(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            iconBg = Color(0xFFFFDAD2).copy(alpha = 0.6f),
                            iconTint = CoralOrangePrimary,
                            title = "Pusat Bantuan & Edukasi",
                            subtitle = "Panduan mendampingi anak membaca",
                            onClick = { }
                        ),
                        SettingsRowData(
                            icon = Icons.Default.Info,
                            iconBg = Color(0xFFD5E3FC),
                            iconTint = Color(0xFF006397),
                            title = "Tentang LITUTUR",
                            subtitle = "Membangun generasi gemar membaca",
                            badgeText = "v1.0.4 Ramah Anak",
                            badgeColor = Color(0xFF64748B),
                            badgeBg = Color(0xFFF8F9FD),
                            onClick = { }
                        )
                    )
                )
            }

            // 6. Logout Action & Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFDAD6))
                            .clickable { onLogout() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keluar dari Akun",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBA1A1A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ID Akun Keluarga: LIT-9824-JKT • Server Aman",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

private data class SettingsRowData(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val badgeText: String? = null,
    val badgeColor: Color = Color.Unspecified,
    val badgeBg: Color = Color.Unspecified,
    val onClick: () -> Unit = {}
)

@Composable
private fun SettingsSectionGroup(
    title: String,
    items: List<SettingsRowData>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    SettingsRow(data = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(data: SettingsRowData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { data.onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(data.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (data.badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(data.badgeBg)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = data.badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = data.badgeColor
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentSettingsScreenPreview() {
    LituturTheme {
        ParentSettingsScreen()
    }
}
