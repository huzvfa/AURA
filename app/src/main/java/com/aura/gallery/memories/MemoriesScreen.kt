package com.aura.gallery.memories

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.gallery.data.MediaItem
import com.aura.gallery.util.Format
import com.aura.gallery.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay

@Composable
fun MemoriesScreen(vm: GalleryViewModel, onBack: () -> Unit) {
    val all by vm.all.collectAsState()
    val favs by vm.favorites.collectAsState()

    val reel: List<MediaItem> = remember(all, favs) {
        val f = all.filter { it.id in favs && !it.isVideo }
        (if (f.size >= 5) f else all.filter { !it.isVideo }).take(24)
    }

    if (reel.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No memories yet", color = Color.White)
                Text("Back", color = Color(0xFF007AFF), modifier = Modifier.padding(top = 12.dp).clickable { onBack() })
            }
        }
        return
    }

    var index by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(true) }

    LaunchedEffect(playing, index, reel.size) {
        if (playing) {
            delay(3500)
            index = (index + 1) % reel.size
        }
    }

    val transition = rememberInfiniteTransition(label = "ken")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Crossfade(targetState = index, label = "reel") { i ->
            AsyncImage(
                model = reel[i].uri, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().scale(scale)
            )
        }
        // gradient scrims
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0x66000000), Color.Transparent, Color(0x99000000)))
        ))

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 42.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement_SpaceBetween
        ) {
            Icon(Icons.Filled.Close, "Close", tint = Color.White,
                modifier = Modifier.size(26.dp).clickable { onBack() })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("Add Music", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text("For You", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Text(Format.date(reel[index].dateTaken), color = Color(0xCCFFFFFF), fontSize = 14.sp)
        }

        Box(
            Modifier.align(Alignment.BottomEnd).padding(20.dp).size(48.dp)
                .background(Color(0x55000000), androidx.compose.foundation.shape.CircleShape)
                .clickable { playing = !playing },
            contentAlignment = Alignment.Center
        ) {
            Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play/Pause",
                tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

private val Arrangement_SpaceBetween = androidx.compose.foundation.layout.Arrangement.SpaceBetween
