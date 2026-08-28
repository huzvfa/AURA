package com.aura.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aura.gallery.ui.PhotosApp
import com.aura.gallery.ui.theme.AuraGalleryTheme
import com.aura.gallery.viewmodel.GalleryViewModel

class MainActivity : ComponentActivity() {

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    private fun hasAnyMediaAccess(): Boolean {
        val perms = requiredPermissions().toMutableList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            perms.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }
        return perms.any {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: GalleryViewModel = viewModel()
            val settings by vm.settings.collectAsState()
            AuraGalleryTheme(themePref = settings.themePref) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Gate(vm)
                }
            }
        }
    }

    @Composable
    private fun Gate(vm: GalleryViewModel) {
        val context = LocalContext.current
        var granted by remember { mutableStateOf(hasAnyMediaAccess()) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.values.any { it }) {
                granted = true
                vm.load()
            }
        }

        if (granted) {
            LaunchedEffect(Unit) { vm.load() }
            PhotosApp(vm)
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Gallery", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Allow access to your photos, videos and media to browse your library.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Button(onClick = {
                    val perms = requiredPermissions().toMutableList()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        perms.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    }
                    launcher.launch(perms.toTypedArray())
                }) { Text("Allow Access") }
            }
        }
    }
}
