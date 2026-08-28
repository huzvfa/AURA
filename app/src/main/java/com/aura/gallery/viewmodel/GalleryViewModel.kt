package com.aura.gallery.viewmodel

import android.app.Application
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aura.gallery.GalleryApp
import com.aura.gallery.data.Album
import com.aura.gallery.data.MediaItem
import com.aura.gallery.data.MediaSection
import com.aura.gallery.data.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Timeline { YEARS, MONTHS, ALL }
enum class MediaFilter { ALL, FAVORITES, EDITED, PHOTOS, VIDEOS, SCREENSHOTS }

class GalleryViewModel(app: Application) : AndroidViewModel(app) {

    private val appCtx = app as GalleryApp
    private val repo = appCtx.mediaRepo
    private val favStore = appCtx.favorites
    private val settingsStore = appCtx.settings

    private val _all = MutableStateFlow<List<MediaItem>>(emptyList())
    val all: StateFlow<List<MediaItem>> = _all.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _sections = MutableStateFlow<List<MediaSection>>(emptyList())
    val sections: StateFlow<List<MediaSection>> = _sections.asStateFlow()

    private val _timeline = MutableStateFlow(Timeline.ALL)
    val timeline: StateFlow<Timeline> = _timeline.asStateFlow()

    private val _filter = MutableStateFlow(MediaFilter.ALL)
    val filter: StateFlow<MediaFilter> = _filter.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Long>>(emptySet())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val settings: StateFlow<Settings> =
        settingsStore.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    init {
        viewModelScope.launch { favStore.favorites.collect { _favorites.value = it } }
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            val items = repo.loadAll()
            _all.value = items
            _albums.value = repo.albums(items)
            recompute()
            _loading.value = false
        }
    }

    fun setTimeline(t: Timeline) { _timeline.value = t; recompute() }
    fun setFilter(f: MediaFilter) { _filter.value = f; recompute() }

    private fun filtered(): List<MediaItem> {
        val favs = _favorites.value
        return when (_filter.value) {
            MediaFilter.ALL -> _all.value
            MediaFilter.FAVORITES -> _all.value.filter { it.id in favs }
            MediaFilter.PHOTOS -> _all.value.filter { !it.isVideo }
            MediaFilter.VIDEOS -> _all.value.filter { it.isVideo }
            MediaFilter.SCREENSHOTS -> _all.value.filter {
                it.bucketName.contains("Screenshot", true) || it.name.contains("Screenshot", true)
            }
            MediaFilter.EDITED -> _all.value.filter { it.name.contains("edit", true) }
        }
    }

    private fun recompute() {
        val items = filtered()
        _sections.value = when (_timeline.value) {
            Timeline.YEARS -> repo.byYear(items)
            Timeline.MONTHS -> repo.byMonth(items)
            Timeline.ALL -> listOf(MediaSection("", items))
        }
    }

    fun toggleFavorite(id: Long) { viewModelScope.launch { favStore.toggle(id) } }

    fun favoriteItems(): List<MediaItem> {
        val favs = _favorites.value
        return _all.value.filter { it.id in favs }
    }

    fun itemsForAlbum(bucketId: Long): List<MediaItem> =
        _all.value.filter { it.bucketId == bucketId }

    fun shareIntent(items: List<MediaItem>): Intent {
        return if (items.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = if (items[0].isVideo) "video/*" else "image/*"
                putExtra(Intent.EXTRA_STREAM, items[0].uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(items.map { it.uri }))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    fun deleteRequest(items: List<MediaItem>): IntentSender? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val uris = items.map { it.uri }
        return MediaStore.createDeleteRequest(getApplication<Application>().contentResolver, uris).intentSender
    }

    fun refreshAfterDelete() = load()

    fun setTheme(v: Int) = viewModelScope.launch { settingsStore.setTheme(v) }
    fun setAutoPlay(v: Boolean) = viewModelScope.launch { settingsStore.setAutoPlay(v) }
    fun setLoop(v: Boolean) = viewModelScope.launch { settingsStore.setLoop(v) }
    fun setHdr(v: Boolean) = viewModelScope.launch { settingsStore.setHdr(v) }
    fun setAspect(v: Boolean) = viewModelScope.launch { settingsStore.setAspect(v) }
    fun setLocation(v: Boolean) = viewModelScope.launch { settingsStore.setLocation(v) }
}
