package br.edu.checkpoint.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Preferences DataStore wrapper for app settings.
 */

private const val DS_NAME = "checkpoint_settings"

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DS_NAME)

private object Keys {
    val DEFAULT_ZOOM = floatPreferencesKey("default_zoom")
    val MAP_TYPE     = intPreferencesKey("map_type")
    val USE_DEVICE_GEOCODER = booleanPreferencesKey("use_device_geocoder")
}

class SettingsStore(private val context: Context) {

    /** Reactive settings stream */
    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            defaultZoom = prefs[Keys.DEFAULT_ZOOM] ?: 14f,
            mapType = MapTypeSetting.fromId(prefs[Keys.MAP_TYPE] ?: MapTypeSetting.NORMAL.id),
            useDeviceGeocoder = prefs[Keys.USE_DEVICE_GEOCODER] ?: true
        )
    }

    /** Setters (each is atomic) */
    suspend fun setDefaultZoom(zoom: Float) {
        context.settingsDataStore.edit { it[Keys.DEFAULT_ZOOM] = zoom.coerceIn(1f, 21f) }
    }

    suspend fun setMapType(type: MapTypeSetting) {
        context.settingsDataStore.edit { it[Keys.MAP_TYPE] = type.id }
    }

    suspend fun setUseDeviceGeocoder(use: Boolean) {
        context.settingsDataStore.edit { it[Keys.USE_DEVICE_GEOCODER] = use }
    }
}
