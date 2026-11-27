package com.example.litutur.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.litutur.data.model.CommunityPostEntity
import com.example.litutur.ui.components.ParentTopBar
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.LituturTheme
import com.example.litutur.ui.theme.SecondaryBrandGreen
import com.example.litutur.ui.viewmodel.ParentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentCommunityScreen(
    uiState: ParentUiState = ParentUiState(),
    onRefresh: () -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onLikePost: (CommunityPostEntity) -> Unit = {},
    onOpenPostDetail: (CommunityPostEntity) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    val currentUid = uiState.parentUser?.uid.orEmpty()

    val filterChips = listOf("Semua", "Milestone", "Tips & Cerita")

    val filteredPosts = uiState.communityPosts.filter { post ->
        val matchesSearch = searchQuery.isBlank() ||
            post.caption.contains(searchQuery, ignoreCase = true) ||
            post.authorName.contains(searchQuery, ignoreCase = true) ||
            post.milestoneTitle.orEmpty().contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Milestone" -> !post.milestoneTitle.isNullOrBlank()
            "Tips & Cerita" -> post.milestoneTitle.isNullOrBlank()
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            ParentTopBar(title = "Komunitas Orang Tua") {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Muat ulang", tint = CoralOrangePrimary)
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreatePost,
                containerColor = CoralOrangePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(50.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Bagikan Milestone", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = Color(0xFFF8F9FD)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari cerita, tips, atau milestone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = CoralOrangePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterChips) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            if (filteredPosts.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada postingan di komunitas.",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Jadilah orang tua pertama yang membagikan pencapaian literasi si kecil!",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    val isLiked = currentUid.isNotBlank() && post.likedBy.contains(currentUid)
                    CommunityPostCard(
                        post = post,
                        isLiked = isLiked,
                        onLikeClick = { onLikePost(post) },
                        onClick = { onOpenPostDetail(post) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPostEntity,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = CoralOrangePrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName.ifBlank { "Orang Tua Litutur" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        if (post.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SecondaryBrandGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Text(
                        text = post.childInfo.ifBlank { post.authorTitle },
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Caption
            Text(
                text = post.caption,
                fontSize = 14.sp,
                color = Color(0xFF1E293B),
                lineHeight = 20.sp
            )

            // Uploaded Cloudinary Image (if any)
            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Foto Postingan",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }

            // Automatic Milestone Attachment Card (Fungsi 4 Problem Solving)
            if (!post.milestoneTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.milestoneTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF065F46)
                        )
                        if (!post.milestoneSubtitle.isNullOrBlank()) {
                            Text(
                                text = post.milestoneSubtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        if (!post.milestoneStats.isNullOrBlank()) {
                            Text(
                                text = post.milestoneStats,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF064E3B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Like & Comment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onLikeClick)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Suka",
                        tint = if (isLiked) Color(0xFFEF4444) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount} Suka",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Komentar",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.commentsCount} Komentar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentCommunityScreenPreview() {
    LituturTheme {
        ParentCommunityScreen()
    }
}
