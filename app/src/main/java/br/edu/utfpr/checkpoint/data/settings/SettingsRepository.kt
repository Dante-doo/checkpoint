package br.edu.checkpoint.data.settings

import kotlinx.coroutines.flow.Flow

/**
 * Thin repository (kept for future growth).
 */
class SettingsRepository(
    private val store: SettingsStore
) {
    val settings: Flow<AppSettings> = store.settings

    suspend fun updateDefaultZoom(zoom: Float) = store.setDefaultZoom(zoom)
    suspend fun updateMapType(type: MapTypeSetting) = store.setMapType(type)
    suspend fun updateUseDeviceGeocoder(use: Boolean) = store.setUseDeviceGeocoder(use)
}
