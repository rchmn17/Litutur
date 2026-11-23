package com.example.litutur.ui.parent

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.litutur.ui.theme.LituturTheme

data class CatalogStory(
    val id: String,
    val title: String,
    val origin: String,
    val moralValue: String,
    val ageRating: String,
    val isRecommended: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentCatalogScreen() {
    var searchQuery by remember { mutableStateOf("") }

    val stories = remember {
        listOf(
            CatalogStory("1", "Timun Mas", "Jawa Tengah", "Keberanian & Ikhtiar", "Usia 6+"),
            CatalogStory("2", "Si Kancil & Buaya", "Sumatera", "Kecerdikan Mengatasi Masalah", "Usia 4+"),
            CatalogStory("3", "Batu Menangis", "Kalimantan Barat", "Penyesalan & Hormat Orang Tua", "Usia 7+"),
            CatalogStory("4", "Malin Kundang", "Sumatera Barat", "Berbakti Kepada Ibu", "Usia 7+"),
            CatalogStory("5", "Lutung Kasarung", "Jawa Barat", "Ketulusan Hati & Kebaikan", "Usia 6+"),
            CatalogStory("6", "Cindelaras", "Jawa Timur", "Kebenaran Selalu Menang", "Usia 6+")
        )
    }

    val storyToggles = remember {
        mutableStateMapOf<String, Boolean>().apply {
            stories.forEach { put(it.id, true) }
        }
    }

    val filteredStories = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            stories
        } else {
            stories.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.origin.contains(searchQuery, ignoreCase = true) ||
                        it.moralValue.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kurasi Cerita & Dongeng Anak",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari judul, daerah, atau nilai moral...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Daftar Cerita Tersedia (${filteredStories.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(filteredStories) { story ->
                val isVisibleToChild = storyToggles[story.id] ?: true
                CatalogStoryCard(
                    story = story,
                    isVisibleToChild = isVisibleToChild,
                    onToggleVisibility = { isChecked ->
                        storyToggles[story.id] = isChecked
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun CatalogStoryCard(
    story: CatalogStory,
    isVisibleToChild: Boolean,
    onToggleVisibility: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVisibleToChild) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isVisibleToChild) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = if (isVisibleToChild) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${story.origin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Pesan Moral: ${story.moralValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${story.ageRating} • " + if (isVisibleToChild) "Aktif untuk Anak" else "Disembunyikan",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isVisibleToChild) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }

            Switch(
                checked = isVisibleToChild,
                onCheckedChange = onToggleVisibility
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentCatalogScreenPreview() {
    LituturTheme {
        ParentCatalogScreen()
    }
}
