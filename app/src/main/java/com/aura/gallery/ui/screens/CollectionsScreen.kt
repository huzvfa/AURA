package com.aura.gallery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
import com.aura.gallery.data.MediaItem
import com.aura.gallery.viewmodel.GalleryViewModel

@Composable
fun CollectionsScreen(
    vm: GalleryViewModel,
    onBack: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenPeople: () -> Unit,
    onOpenMemories: () -> Unit,
    onOpenAlbum: (Long, String) -> Unit
) {
    val albums by vm.albums.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar("Collections", onBack)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item { BigTile("Memories", Color(0xFF5856D6), onOpenMemories) }
            item { BigTile("People & Pets", Color(0xFF34C759), onOpenPeople) }
            item { BigTile("All Albums", Color(0xFF007AFF), onOpenAlbums) }
            item { BigTile("Recent Days", Color(0xFFFF9500)) {} }
            items(albums.size) { i ->
                val a = albums[i]
                Column(Modifier.clickable { onOpenAlbum(a.id, a.name) }) {
                    AsyncImage(
                        model = a.coverUri, contentDescription = a.name, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFDDDDDD))
                    )
                    Text(a.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun BigTile(title: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f))))
            .clickable { onClick() }
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
    }
}

@Composable
fun SearchScreen(vm: GalleryViewModel, onBack: () -> Unit, onOpenViewer: (List<MediaItem>, Int) -> Unit) {
    val all by vm.all.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar("Search", onBack)
        val cats = listOf(
            "Videos" to all.filter { it.isVideo },
            "Photos" to all.filter { !it.isVideo },
            "Recents" to all.take(60)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(cats.size) { i ->
                val (label, list) = cats[i]
                Box(
                    Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFCCCCCC))
                        .clickable { if (list.isNotEmpty()) onOpenViewer(list, 0) }
                ) {
                    list.firstOrNull()?.let {
                        AsyncImage(model = it.uri, contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                    }
                    Box(Modifier.fillMaxSize().background(Color(0x33000000)))
                    Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.BottomStart).padding(10.dp))
                }
            }
        }
    }
}
