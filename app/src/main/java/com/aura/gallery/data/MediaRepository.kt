package com.aura.gallery.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class MediaRepository(private val context: Context) {

    /** Loads all images + videos from the device, newest first. */
    suspend fun loadAll(): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = ArrayList<MediaItem>()
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DURATION,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.SIZE
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        context.contentResolver.query(collection, projection, selection, args, sortOrder)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val takenCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val modCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val typeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
            val bidCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val bnameCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val wCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val hCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val isVideo = c.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val taken = c.getLong(takenCol).let { if (it > 0) it else c.getLong(modCol) * 1000 }
                val uri: Uri = ContentUris.withAppendedId(
                    if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                items.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        name = c.getString(nameCol) ?: "",
                        dateTaken = taken,
                        isVideo = isVideo,
                        durationMs = c.getLong(durCol),
                        bucketId = c.getLong(bidCol),
                        bucketName = c.getString(bnameCol) ?: "Unknown",
                        width = c.getInt(wCol),
                        height = c.getInt(hCol),
                        sizeBytes = c.getLong(sizeCol)
                    )
                )
            }
        }
        items
    }

    /** Groups media into albums (by bucket) with a cover + count. */
    fun albums(all: List<MediaItem>): List<Album> {
        return all.groupBy { it.bucketId }
            .map { (bid, list) ->
                val cover = list.maxByOrNull { it.dateTaken }!!
                Album(bid, cover.bucketName, cover.uri, list.size,
                    isVideoAlbum = list.all { it.isVideo })
            }
            .sortedByDescending { it.count }
    }

    /** Sections grouped by month, e.g. "July 2025". */
    fun byMonth(all: List<MediaItem>): List<MediaSection> = group(all, month = true)

    /** Sections grouped by year. */
    fun byYear(all: List<MediaItem>): List<MediaSection> = group(all, month = false)

    private val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private fun group(all: List<MediaItem>, month: Boolean): List<MediaSection> {
        val cal = Calendar.getInstance()
        return all.groupBy {
            cal.timeInMillis = it.dateTaken
            if (month) "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
            else "${cal.get(Calendar.YEAR)}"
        }.map { (title, list) -> MediaSection(title, list) }
    }
}
