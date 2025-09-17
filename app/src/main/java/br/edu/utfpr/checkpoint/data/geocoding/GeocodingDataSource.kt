package br.edu.utfpr.checkpoint.data.geocoding

import android.location.Address

/**
 * Generic reverse geocoding interface for (lat, lng) -> Address line.
 * You can implement it with the device Geocoder or a web API (Retrofit).
 */
interface GeocodingDataSource {
    /**
     * Returns a human-readable single-line address for the given coordinates,
     * or null if it couldn't be resolved.
     */
    suspend fun reverseGeocode(lat: Double, lng: Double): String?
}

/** Helper to flatten Android Address into a single line. */
fun Address.toSingleLine(): String {
    // Build a readable, compact address line.
    val parts = buildList {
        getAddressLine(0)?.let { add(it) }
        // Fallback: compose from components if addressLine not present
        if (isEmpty()) {
            listOfNotNull(subAdminArea, adminArea, countryName).forEach { add(it) }
        }
    }
    return parts.joinToString(separator = ", ").ifBlank { null } ?: return "Unknown"
}
