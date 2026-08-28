package com.aura.gallery.people

import android.content.Context
import com.aura.gallery.data.MediaItem
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Best-effort People & Pets. ML Kit detects faces (not identity), so this
 * surfaces photos that contain people. True per-person grouping needs an
 * on-device embedding model and is the piece that improves with real-device use.
 */
object FaceGrouper {
    suspend fun photosWithFaces(
        context: Context,
        items: List<MediaItem>,
        cap: Int = 100
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val opts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        val detector = FaceDetection.getClient(opts)
        val out = ArrayList<MediaItem>()
        for (item in items.asSequence().filter { !it.isVideo }.take(cap)) {
            try {
                val image = InputImage.fromFilePath(context, item.uri)
                val faces = Tasks.await(detector.process(image))
                if (faces.isNotEmpty()) out.add(item)
            } catch (_: Exception) {
            }
        }
        detector.close()
        out
    }
}
