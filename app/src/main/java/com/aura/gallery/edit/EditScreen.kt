package com.aura.gallery.edit

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.aura.gallery.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun buildValues(tone: Float, color: Float, palette: Float): FloatArray {
    val b = 0.6f + tone * 0.8f
    val s = color * 2f
    val w = (palette - 0.5f) * 0.4f
    val lr = 0.213f; val lg = 0.715f; val lb = 0.072f
    val sr = (1 - s) * lr; val sg = (1 - s) * lg; val sb = (1 - s) * lb
    return floatArrayOf(
        (sr + s) * b, sg * b, sb * b, 0f, w * 255f,
        sr * b, (sg + s) * b, sb * b, 0f, 0f,
        sr * b, sg * b, (sb + s) * b, 0f, -w * 255f,
        0f, 0f, 0f, 1f, 0f
    )
}

@Composable
fun EditScreen(item: MediaItem, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bmp by remember { mutableStateOf<Bitmap?>(null) }
    var tab by remember { mutableStateOf("Tone") }
    var tone by remember { mutableFloatStateOf(0.5f) }
    var color by remember { mutableFloatStateOf(0.5f) }
    var palette by remember { mutableFloatStateOf(0.5f) }

    LaunchedEffect(item.uri) {
        bmp = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(item.uri)?.use { input ->
                    val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                    BitmapFactory.decodeStream(input, null, opts)
                }
            } catch (_: Exception) { null }
        }
    }

    val values = buildValues(tone, color, palette)
    val matrix = ColorMatrix(values.copyOf())

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Cancel", color = Color.White, fontSize = 15.sp, modifier = Modifier.clickable { onClose() })
            Text("Done", color = Color(0xFFFFD60A), fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    val src = bmp
                    if (src != null) {
                        scope.launch {
                            saveEdited(context, src, values)
                            onClose()
                        }
                    } else onClose()
                })
        }

        Box(Modifier.weight(1f).fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
            bmp?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.colorMatrix(matrix),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Tone", "Color", "Palette").forEach { t ->
                val v = when (t) { "Tone" -> tone; "Color" -> color; else -> palette }
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { tab = t }) {
                    Text(t.uppercase(), color = Color(0xFF8E8E93), fontSize = 10.sp)
                    Text("${(v * 100).toInt()}", color = if (tab == t) Color.White else Color(0xFFAAAAAA),
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
        Slider(
            value = when (tab) { "Tone" -> tone; "Color" -> color; else -> palette },
            onValueChange = { when (tab) { "Tone" -> tone = it; "Color" -> color = it; else -> palette = it } },
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(30.dp))
    }
}

private suspend fun saveEdited(context: android.content.Context, src: Bitmap, values: FloatArray) {
    withContext(Dispatchers.IO) {
        try {
            val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(values) }
            canvas.drawBitmap(src, 0f, 0f, paint)

            val name = "edited_${System.currentTimeMillis()}.jpg"
            val values2 = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Gallery")
                }
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values2)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    out.compress(Bitmap.CompressFormat.JPEG, 95, os)
                }
                withContext(Dispatchers.Main) { Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show() }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { Toast.makeText(context, "Couldn't save", Toast.LENGTH_SHORT).show() }
        }
    }
}
