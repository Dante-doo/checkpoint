package br.edu.checkpoint.checkpoint.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Core entity for a tourist spot persisted locally with Room.
 * We keep both coordinates (lat/lng) and a cached textual address (optional).
 */
@Entity(
    tableName = "tourist_spots",
    indices = [
        Index(value = ["name"]),                 // speeds up name search
        Index(value = ["lat", "lng"]),          // helps map bounding queries
    ]
)
data class TouristSpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val description: String? = null,

    val lat: Double,
    val lng: Double,

    /** Optional geocode string you may capture from user or reverse geocoder */
    val geocode: String? = null,

    /** Cached human-readable address (resolved via Geocoder/API) */
    val address: String? = null,

    /** Persisted as String (content:// or file://), handled by a converter if needed */
    val imageUri: String? = null,

    /** Timestamps for sync/audit if needed */
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis(),

    /** If address resolution is pending/offline failed, mark here to retry later */
    val needsAddressResolve: Boolean = false
)
