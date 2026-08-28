package com.aura.gallery.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.gallery.data.MediaItem
import com.aura.gallery.ui.components.PhotoThumb
import com.aura.gallery.viewmodel.GalleryViewModel
import com.aura.gallery.viewmodel.MediaFilter
import com.aura.gallery.viewmodel.Timeline

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    vm: GalleryViewModel,
    onOpenViewer: (List<MediaItem>, Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenPeople: () -> Unit,
    onOpenMemories: () -> Unit
) {
    val all by vm.all.collectAsState()
    val sections by vm.sections.collectAsState()
    val timeline by vm.timeline.collectAsState()
    val filter by vm.filter.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val settings by vm.settings.collectAsState()

    var cols by remember { mutableIntStateOf(3) }
    var sortMenu by remember { mutableStateOf(false) }
    var sub by remember { mutableStateOf("") }        // "", filter, view
    var showOptions by remember { mutableStateOf(false) }
    var selectMode by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<Long>() }
    var contextItem by remember { mutableStateOf<MediaItem?>(null) }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) vm.refreshAfterDelete()
        selectMode = false; selected.clear(); contextItem = null
    }
    fun requestDelete(items: List<MediaItem>) {
        vm.deleteRequest(items)?.let { deleteLauncher.launch(IntentSenderRequest.Builder(it).build()) }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        if (timeline == Timeline.ALL) {
            val list = if (filter == MediaFilter.FAVORITES) all.filter { it.id in favorites } else all
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                contentPadding = PaddingValues(top = 56.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                verticalArrangement = Arrangement.spacedBy(1.5.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CollectionsStrip(onOpenPeople, onOpenMemories, onOpenAlbums)
                }
                items(list.size) { i ->
                    val item = list[i]
                    val isSel = item.id in selected
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .combinedClickable(
                                onClick = {
                                    if (selectMode) { if (isSel) selected.remove(item.id) else selected.add(item.id) }
                                    else onOpenViewer(list, i)
                                },
                                onLongClick = { contextItem = item }
                            )
                    ) {
                        PhotoThumb(
                            item = item,
                            modifier = Modifier.fillMaxSize(),
                            isFavorite = item.id in favorites,
                            selectMode = selectMode,
                            selected = isSel,
                            cornerRadius = if (settings.aspectGrid) 2 else 0
                        )
                    }
                }
            }
        } else {
            // Months / Years feed
            LazyColumn(
                contentPadding = PaddingValues(top = 56.dp, bottom = 120.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                sections.forEach { section ->
                    item {
                        Text(
                            section.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    val chunk = section.items
                    items(chunk.size) { i ->
                        val item = chunk[i]
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .height(210.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .combinedClickable(
                                    onClick = { onOpenViewer(chunk, i) },
                                    onLongClick = { contextItem = item }
                                )
                        ) {
                            AsyncImage(
                                model = item.uri, contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Top-right: search + profile
        if (!selectMode) {
            Row(
                Modifier.align(Alignment.TopEnd).padding(top = 44.dp, end = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCircle { Icon(Icons.Filled.Search, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF8FD694))
                        .combinedClickable(onClick = { showOptions = true }, onLongClick = {}),
                    contentAlignment = Alignment.Center
                ) { Text("Me", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        } else {
            Row(
                Modifier.align(Alignment.TopStart).fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (selected.isEmpty()) "Select Items" else "${selected.size} Selected",
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text("Cancel", color = Color(0xFF007AFF), fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.combinedClickable(onClick = { selectMode = false; selected.clear() }, onLongClick = {}))
            }
        }

        // Bottom floating: sort + Years/Months/All
        if (!selectMode) {
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        GlassCircle(onClick = { sortMenu = true; sub = "" }) {
                            Text("⇅", color = Color.White, fontSize = 18.sp)
                        }
                        SortMenu(
                            expanded = sortMenu, sub = sub, filter = filter,
                            onDismiss = { sortMenu = false },
                            onSub = { sub = it },
                            onFilter = { vm.setFilter(it) },
                            onSelect = { selectMode = true; sortMenu = false }
                        )
                    }
                    GlassPill {
                        SegItem("Years", timeline == Timeline.YEARS) { vm.setTimeline(Timeline.YEARS) }
                        SegItem("Months", timeline == Timeline.MONTHS) { vm.setTimeline(Timeline.MONTHS) }
                        SegItem("All", timeline == Timeline.ALL) { vm.setTimeline(Timeline.ALL) }
                    }
                }
            }
        } else {
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface).padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Share", color = Color(0xFF007AFF),
                    modifier = Modifier.combinedClickable(onClick = {
                        val items = all.filter { it.id in selected }
                        if (items.isNotEmpty()) vm.getApplication<android.app.Application>()
                            .startActivity(android.content.Intent.createChooser(vm.shareIntent(items), "Share").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                    }, onLongClick = {}))
                Text("Favorite", color = Color(0xFF007AFF),
                    modifier = Modifier.combinedClickable(onClick = { selected.forEach { vm.toggleFavorite(it) } }, onLongClick = {}))
                Text("Delete", color = Color(0xFFFF3B30),
                    modifier = Modifier.combinedClickable(onClick = { requestDelete(all.filter { it.id in selected }) }, onLongClick = {}))
            }
        }
    }

    // long-press context menu
    if (contextItem != null) {
        val item = contextItem!!
        ModalBottomSheet(onDismissRequest = { contextItem = null }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.padding(bottom = 24.dp)) {
                ContextRow("Share") {
                    vm.getApplication<android.app.Application>().startActivity(
                        android.content.Intent.createChooser(vm.shareIntent(listOf(item)), "Share")
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    ); contextItem = null
                }
                ContextRow(if (item.id in favorites) "Unfavorite" else "Favorite") { vm.toggleFavorite(item.id); contextItem = null }
                ContextRow("Delete", danger = true) { requestDelete(listOf(item)) }
            }
        }
    }

    // options sheet
    if (showOptions) {
        ModalBottomSheet(onDismissRequest = { showOptions = false }, sheetState = rememberModalBottomSheetState()) {
            OptionsSheetContent(vm)
        }
    }
}

@Composable
private fun CollectionsStrip(onPeople: () -> Unit, onMemories: () -> Unit, onAlbums: () -> Unit) {
    LazyRow(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { CollectionCard("Memories", Color(0xFF5856D6), onMemories) }
        item { CollectionCard("People & Pets", Color(0xFF34C759), onPeople) }
        item { CollectionCard("Albums", Color(0xFF007AFF), onAlbums) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollectionCard(title: String, color: Color, onClick: () -> Unit) {
    Column(
        Modifier.width(120.dp).combinedClickable(onClick = onClick, onLongClick = {})
    ) {
        Box(
            Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(12.dp)).background(color)
        )
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GlassCircle(onClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).background(Color(0x66787880))
            .combinedClickable(onClick = onClick, onLongClick = {}),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun GlassPill(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.height(42.dp).clip(RoundedCornerShape(21.dp)).background(Color(0x66787880))
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SegItem(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(16.dp))
            .background(if (on) Color(0x55FFFFFF) else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = {})
            .padding(horizontal = 15.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color.White, fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun SortMenu(
    expanded: Boolean, sub: String, filter: MediaFilter,
    onDismiss: () -> Unit, onSub: (String) -> Unit,
    onFilter: (MediaFilter) -> Unit, onSelect: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        when (sub) {
            "filter" -> {
                DropdownMenuItem(text = { Text("‹ Filter") }, onClick = { onSub("") })
                listOf(
                    "All Items" to MediaFilter.ALL, "Favorites" to MediaFilter.FAVORITES,
                    "Edited" to MediaFilter.EDITED, "Photos" to MediaFilter.PHOTOS,
                    "Videos" to MediaFilter.VIDEOS, "Screenshots" to MediaFilter.SCREENSHOTS
                ).forEach { (label, f) ->
                    DropdownMenuItem(
                        text = { Text((if (filter == f) "✓ " else "") + label) },
                        onClick = { onFilter(f); onDismiss() }
                    )
                }
            }
            "view" -> {
                DropdownMenuItem(text = { Text("‹ View Options") }, onClick = { onSub("") })
                DropdownMenuItem(text = { Text("Zoom: double-tap grid") }, onClick = { onDismiss() })
            }
            else -> {
                DropdownMenuItem(text = { Text("✓ Sort by Date Captured") }, onClick = { onDismiss() })
                DropdownMenuItem(text = { Text("Sort by Recently Added") }, onClick = { onDismiss() })
                DropdownMenuItem(text = { Text("Filter ›") }, onClick = { onSub("filter") })
                DropdownMenuItem(text = { Text("View Options ›") }, onClick = { onSub("view") })
                DropdownMenuItem(text = { Text("Select") }, onClick = onSelect)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContextRow(label: String, danger: Boolean = false, onClick: () -> Unit) {
    Text(
        label,
        color = if (danger) Color(0xFFFF3B30) else MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = {}).padding(16.dp)
    )
}

@Composable
fun OptionsSheetContent(vm: GalleryViewModel) {
    val all by vm.all.collectAsState()
    val settings by vm.settings.collectAsState()
    val photos = all.count { !it.isVideo }
    val videos = all.count { it.isVideo }
    Column(Modifier.padding(16.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF8FD694)), contentAlignment = Alignment.Center) {
                    Text("Me", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Text("My Library", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
                    modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurface)
                Text("$photos Photos, $videos Videos", color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("VIEW OPTIONS", fontSize = 11.sp, color = Color(0xFF8E8E93), modifier = Modifier.padding(vertical = 6.dp))
        ToggleRow("Auto-Play Motion", settings.autoPlayMotion) { vm.setAutoPlay(it) }
        ToggleRow("Loop Videos", settings.loopVideos) { vm.setLoop(it) }
        ToggleRow("View Full HDR", settings.fullHdr) { vm.setHdr(it) }
        ToggleRow("Aspect Ratio Grid", settings.aspectGrid) { vm.setAspect(it) }
        ToggleRow("Show Location", settings.showLocation) { vm.setLocation(it) }
        Spacer(Modifier.height(10.dp))
        Text("APPEARANCE", fontSize = 11.sp, color = Color(0xFF8E8E93), modifier = Modifier.padding(vertical = 6.dp))
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip("System", settings.themePref == 0) { vm.setTheme(0) }
            ThemeChip("Light", settings.themePref == 1) { vm.setTheme(1) }
            ThemeChip("Dark", settings.themePref == 2) { vm.setTheme(2) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemeChip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (on) Color(0xFF007AFF) else Color(0x22787880))
            .combinedClickable(onClick = onClick, onLongClick = {})
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (on) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
    }
}
