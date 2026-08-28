package com.aura.gallery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aura.gallery.data.MediaItem
import com.aura.gallery.edit.EditScreen
import com.aura.gallery.memories.MemoriesScreen
import com.aura.gallery.ui.screens.AlbumDetailScreen
import com.aura.gallery.ui.screens.AlbumsScreen
import com.aura.gallery.ui.screens.CollectionsScreen
import com.aura.gallery.ui.screens.LibraryScreen
import com.aura.gallery.ui.screens.PeopleScreen
import com.aura.gallery.ui.screens.SearchScreen
import com.aura.gallery.ui.screens.ViewerScreen
import com.aura.gallery.viewmodel.GalleryViewModel

sealed class Screen {
    object Library : Screen()
    object Collections : Screen()
    object Albums : Screen()
    object Search : Screen()
    object People : Screen()
    object Memories : Screen()
    data class AlbumDetail(val bucketId: Long, val title: String) : Screen()
}

data class ViewerState(val items: List<MediaItem>, val index: Int)

@Composable
fun PhotosApp(vm: GalleryViewModel) {
    var screen by remember { mutableStateOf<Screen>(Screen.Library) }
    var viewer by remember { mutableStateOf<ViewerState?>(null) }
    var editItem by remember { mutableStateOf<MediaItem?>(null) }

    val openViewer: (List<MediaItem>, Int) -> Unit = { list, i -> viewer = ViewerState(list, i) }

    Box(Modifier.fillMaxSize()) {
        when (val s = screen) {
            is Screen.Library -> LibraryScreen(
                vm = vm,
                onOpenViewer = openViewer,
                onOpenSearch = { screen = Screen.Search },
                onOpenCollections = { screen = Screen.Collections },
                onOpenAlbums = { screen = Screen.Albums },
                onOpenPeople = { screen = Screen.People },
                onOpenMemories = { screen = Screen.Memories }
            )
            is Screen.Collections -> CollectionsScreen(
                vm = vm,
                onBack = { screen = Screen.Library },
                onOpenAlbums = { screen = Screen.Albums },
                onOpenPeople = { screen = Screen.People },
                onOpenMemories = { screen = Screen.Memories },
                onOpenAlbum = { id, t -> screen = Screen.AlbumDetail(id, t) }
            )
            is Screen.Albums -> AlbumsScreen(
                vm = vm,
                onBack = { screen = Screen.Collections },
                onOpenAlbum = { id, t -> screen = Screen.AlbumDetail(id, t) }
            )
            is Screen.AlbumDetail -> AlbumDetailScreen(
                vm = vm, bucketId = s.bucketId, title = s.title,
                onBack = { screen = Screen.Albums },
                onOpenViewer = openViewer
            )
            is Screen.Search -> SearchScreen(
                vm = vm, onBack = { screen = Screen.Library }, onOpenViewer = openViewer
            )
            is Screen.People -> PeopleScreen(vm = vm, onBack = { screen = Screen.Collections })
            is Screen.Memories -> MemoriesScreen(vm = vm, onBack = { screen = Screen.Collections })
        }

        // Viewer overlay
        AnimatedVisibility(
            visible = viewer != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            viewer?.let { vs ->
                ViewerScreen(
                    vm = vm,
                    items = vs.items,
                    startIndex = vs.index,
                    onClose = { viewer = null },
                    onEdit = { editItem = it }
                )
            }
        }

        // Edit overlay
        AnimatedVisibility(
            visible = editItem != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            editItem?.let { item ->
                EditScreen(item = item, onClose = { editItem = null })
            }
        }
    }
}
