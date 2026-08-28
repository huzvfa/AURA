package com.aura.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.aura.gallery.util.Format

@Composable
fun PhotoThumb(
    item: MediaItem,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    selectMode: Boolean = false,
    selected: Boolean = false,
    cornerRadius: Int = 0,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(Color(0xFFDDDDDD))
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.name,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
        if (item.isVideo) {
            Icon(
                Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(16.dp)
            )
            Text(
                Format.duration(item.durationMs),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp)
            )
        }
        if (isFavorite && !selectMode) {
            Icon(
                Icons.Filled.Favorite, contentDescription = null, tint = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).size(14.dp)
            )
        }
        if (selectMode) {
            Box(
                Modifier.align(Alignment.BottomEnd).padding(5.dp).size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color(0xFF007AFF) else Color(0x55000000))
            ) {
                if (selected) Icon(
                    Icons.Filled.Check, contentDescription = null, tint = Color.White,
                    modifier = Modifier.align(Alignment.Center).size(15.dp)
                )
            }
        }
    }
}
