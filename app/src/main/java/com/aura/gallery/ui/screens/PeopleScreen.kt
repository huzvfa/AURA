package com.aura.gallery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.gallery.data.MediaItem
import com.aura.gallery.people.FaceGrouper
import com.aura.gallery.viewmodel.GalleryViewModel

@Composable
fun PeopleScreen(vm: GalleryViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val all by vm.all.collectAsState()
    var loading by remember { mutableStateOf(true) }
    var faces by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    LaunchedEffect(all.size) {
        if (all.isNotEmpty()) {
            loading = true
            faces = FaceGrouper.photosWithFaces(context, all)
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar("People & Pets", onBack)
        Text(
            "Photos with people, detected on-device. Grouping by identity improves the more you use it.",
            fontSize = 12.sp, color = Color8E, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Scanning your photos…", modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(faces.size) { i ->
                    Box(Modifier.aspectRatio(1f)) {
                        AsyncImage(
                            model = faces[i].uri, contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

private val Color8E = androidx.compose.ui.graphics.Color(0xFF8E8E93)
