package com.aura.gallery.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favDataStore by preferencesDataStore(name = "gallery_favorites")

class FavoritesStore(private val context: Context) {
    private val KEY = stringSetPreferencesKey("favorite_ids")

    val favorites: Flow<Set<Long>> = context.favDataStore.data.map { prefs ->
        (prefs[KEY] ?: emptySet()).mapNotNull { it.toLongOrNull() }.toSet()
    }

    suspend fun toggle(id: Long) {
        context.favDataStore.edit { prefs ->
            val cur = (prefs[KEY] ?: emptySet()).toMutableSet()
            val s = id.toString()
            if (!cur.add(s)) cur.remove(s)
            prefs[KEY] = cur
        }
    }
}
