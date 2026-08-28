package com.aura.gallery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.gallery.data.MediaItem
import com.aura.gallery.viewmodel.GalleryViewModel

@Composable
fun ScreenTopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 44.dp, start = 12.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF007AFF),
            modifier = Modifier.size(26.dp).clickable { onBack() })
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun AlbumsScreen(vm: GalleryViewModel, onBack: () -> Unit, onOpenAlbum: (Long, String) -> Unit) {
    val albums by vm.albums.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar("Albums", onBack)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums.size) { i ->
                val a = albums[i]
                Column(Modifier.clickable { onOpenAlbum(a.id, a.name) }) {
                    AsyncImage(
                        model = a.coverUri, contentDescription = a.name, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFDDDDDD))
                    )
                    Text(a.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 6.dp))
                    Text("${a.count}", color = Color(0xFF8E8E93), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AlbumDetailScreen(
    vm: GalleryViewModel, bucketId: Long, title: String,
    onBack: () -> Unit, onOpenViewer: (List<MediaItem>, Int) -> Unit
) {
    val all by vm.all.collectAsState()
    val list = all.filter { it.bucketId == bucketId }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title, onBack)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
            verticalArrangement = Arrangement.spacedBy(1.5.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list.size) { i ->
                Box(Modifier.aspectRatio(1f).clickable { onOpenViewer(list, i) }) {
                    AsyncImage(
                        model = list[i].uri, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().background(Color(0xFFDDDDDD))
                    )
                }
            }
        }
    }
}
