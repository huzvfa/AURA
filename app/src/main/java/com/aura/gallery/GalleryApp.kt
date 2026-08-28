package com.aura.gallery

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.aura.gallery.data.FavoritesStore
import com.aura.gallery.data.MediaRepository
import com.aura.gallery.data.SettingsStore

class GalleryApp : Application(), ImageLoaderFactory {
    lateinit var mediaRepo: MediaRepository
        private set
    lateinit var favorites: FavoritesStore
        private set
    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        mediaRepo = MediaRepository(this)
        favorites = FavoritesStore(this)
        settings = SettingsStore(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(VideoFrameDecoder.Factory()) }
            .crossfade(true)
            .build()

    companion object {
        lateinit var instance: GalleryApp
            private set
    }
}
