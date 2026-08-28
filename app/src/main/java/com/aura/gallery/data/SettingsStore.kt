package com.aura.gallery.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "gallery_settings")

data class Settings(
    val themePref: Int = 0,          // 0 follow system, 1 light, 2 dark
    val autoPlayMotion: Boolean = true,
    val loopVideos: Boolean = true,
    val fullHdr: Boolean = true,
    val aspectGrid: Boolean = false,
    val showLocation: Boolean = true
)

class SettingsStore(private val context: Context) {
    private val THEME = intPreferencesKey("theme")
    private val AUTOPLAY = booleanPreferencesKey("autoplay")
    private val LOOP = booleanPreferencesKey("loop")
    private val HDR = booleanPreferencesKey("hdr")
    private val ASPECT = booleanPreferencesKey("aspect")
    private val LOC = booleanPreferencesKey("loc")

    val settings: Flow<Settings> = context.settingsDataStore.data.map { p ->
        Settings(
            themePref = p[THEME] ?: 0,
            autoPlayMotion = p[AUTOPLAY] ?: true,
            loopVideos = p[LOOP] ?: true,
            fullHdr = p[HDR] ?: true,
            aspectGrid = p[ASPECT] ?: false,
            showLocation = p[LOC] ?: true
        )
    }

    suspend fun setTheme(v: Int) = context.settingsDataStore.edit { it[THEME] = v }
    suspend fun setAutoPlay(v: Boolean) = context.settingsDataStore.edit { it[AUTOPLAY] = v }
    suspend fun setLoop(v: Boolean) = context.settingsDataStore.edit { it[LOOP] = v }
    suspend fun setHdr(v: Boolean) = context.settingsDataStore.edit { it[HDR] = v }
    suspend fun setAspect(v: Boolean) = context.settingsDataStore.edit { it[ASPECT] = v }
    suspend fun setLocation(v: Boolean) = context.settingsDataStore.edit { it[LOC] = v }
}
