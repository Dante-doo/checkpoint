package br.edu.checkpoint.data.settings

import com.google.android.gms.maps.GoogleMap

/**
 * Immutable settings snapshot.
 */
data class AppSettings(
    val defaultZoom: Float = 14f,
    val mapType: MapTypeSetting = MapTypeSetting.NORMAL,
    val useDeviceGeocoder: Boolean = true // we keep it for clarity even if always true for now
)

/**
 * Persistable map type options.
 */
enum class MapTypeSetting(val id: Int) {
    NORMAL(GoogleMap.MAP_TYPE_NORMAL),
    SATELLITE(GoogleMap.MAP_TYPE_SATELLITE),
    TERRAIN(GoogleMap.MAP_TYPE_TERRAIN),
    HYBRID(GoogleMap.MAP_TYPE_HYBRID);

    companion object {
        fun fromId(id: Int): MapTypeSetting =
            entries.firstOrNull { it.id == id } ?: NORMAL
    }
}
