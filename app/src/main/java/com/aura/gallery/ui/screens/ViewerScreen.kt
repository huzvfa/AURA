package com.aura.gallery.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.gallery.data.MediaItem
import com.aura.gallery.util.Format
import com.aura.gallery.viewmodel.GalleryViewModel
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewerScreen(
    vm: GalleryViewModel,
    items: List<MediaItem>,
    startIndex: Int,
    onClose: () -> Unit,
    onEdit: (MediaItem) -> Unit
) {
    if (items.isEmpty()) { onClose(); return }
    val context = LocalContext.current
    val favorites by vm.favorites.collectAsState()
    val pagerState = rememberPagerState(initialPage = startIndex.coerceIn(0, items.size - 1)) { items.size }
    var moreMenu by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    val current = items[pagerState.currentPage.coerceIn(0, items.size - 1)]

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { res -> if (res.resultCode == Activity.RESULT_OK) { vm.refreshAfterDelete(); onClose() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val item = items[page]
            val zoom = rememberZoomState()
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().zoomable(zoom)
                )
                if (item.isVideo) {
                    Box(
                        Modifier.size(64.dp).clip(RoundedCornerShape(32.dp)).background(Color(0x55000000))
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(item.uri, "video/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                })
                            },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                }
            }
        }

        // top bar
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .background(Color(0x66000000)).padding(top = 40.dp, start = 12.dp, end = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White,
                modifier = Modifier.size(26.dp).clickable { onClose() })
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Format.date(current.dateTaken), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(Format.time(current.dateTaken), color = Color(0xCCFFFFFF), fontSize = 12.sp)
            }
            Box {
                Icon(Icons.Filled.MoreHoriz, "More", tint = Color.White,
                    modifier = Modifier.size(24.dp).clickable { moreMenu = true })
                DropdownMenu(expanded = moreMenu, onDismissRequest = { moreMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { moreMenu = false; onEdit(current) })
                    DropdownMenuItem(text = { Text("Share") }, onClick = {
                        moreMenu = false
                        context.startActivity(Intent.createChooser(vm.shareIntent(listOf(current)), "Share"))
                    })
                    DropdownMenuItem(text = { Text("Info") }, onClick = { moreMenu = false; showInfo = true })
                }
            }
        }

        // bottom bar
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color(0x66000000)).padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Share, "Share", tint = Color.White,
                modifier = Modifier.size(24.dp).clickable {
                    context.startActivity(Intent.createChooser(vm.shareIntent(listOf(current)), "Share"))
                })
            val fav = current.id in favorites
            Icon(if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Favorite",
                tint = if (fav) Color(0xFFFF3B30) else Color.White,
                modifier = Modifier.size(24.dp).clickable { vm.toggleFavorite(current.id) })
            Icon(Icons.Filled.Info, "Info", tint = Color.White,
                modifier = Modifier.size(24.dp).clickable { showInfo = !showInfo })
            Icon(Icons.Filled.Delete, "Delete", tint = Color.White,
                modifier = Modifier.size(24.dp).clickable {
                    vm.deleteRequest(listOf(current))?.let {
                        deleteLauncher.launch(IntentSenderRequest.Builder(it).build())
                    }
                })
        }

        // info panel
        AnimatedVisibility(
            visible = showInfo,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xF01C1C1E)).padding(18.dp)
            ) {
                Text(Format.date(current.dateTaken), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${Format.weekday(current.dateTaken)} · ${Format.time(current.dateTaken)}",
                    color = Color(0xB3FFFFFF), fontSize = 13.sp)
                Text(
                    buildString {
                        append(if (current.isVideo) "Video" else "Photo")
                        if (current.width > 0) append(" · ${current.width}×${current.height}")
                        append(" · ${current.sizeBytes / 1024 / 1024} MB")
                    },
                    color = Color(0xB3FFFFFF), fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(current.bucketName, color = Color(0x99FFFFFF), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
