package br.edu.utfpr.checkpoint.data.geocoding

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Reverse geocoding using the device Geocoder (may require network but no API key).
 * Works best when Google Play services / backend are available on the device.
 */
class DeviceGeocoderDataSource(
    context: Context,
    locale: Locale = Locale.getDefault()
) : GeocodingDataSource {

    private val geocoder = Geocoder(context.applicationContext, locale)

    override suspend fun reverseGeocode(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        try {
            val list = geocoder.getFromLocation(lat, lng, /*maxResults=*/ 1)
            val addr = list?.firstOrNull() ?: return@withContext null
            addr.toSingleLine()
        } catch (e: Exception) {
            null
        }
    }
}
