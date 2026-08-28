package com.aura.gallery.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateTaken: Long,      // millis
    val isVideo: Boolean,
    val durationMs: Long,     // videos only
    val bucketId: Long,       // album id
    val bucketName: String,   // album name
    val width: Int,
    val height: Int,
    val sizeBytes: Long
)

data class Album(
    val id: Long,
    val name: String,
    val coverUri: Uri,
    val count: Int,
    val isVideoAlbum: Boolean = false
)

/** A group of media under a date header (month or year). */
data class MediaSection(
    val title: String,
    val items: List<MediaItem>
)
