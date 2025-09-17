package br.edu.utfpr.checkpoint.data.repository

import br.edu.checkpoint.checkpoint.data.model.TouristSpotEntity
import br.edu.utfpr.checkpoint.data.db.TouristSpotDao
import br.edu.utfpr.checkpoint.data.geocoding.GeocodingDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Repository orchestrates local DB (Room) and reverse geocoding.
 * It is offline-first: CRUD works locally; address resolution is best-effort.
 */
class TouristSpotRepository(
    private val dao: TouristSpotDao,
    private val geocoding: GeocodingDataSource,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {

    /** Observe all spots ordered by name (for list/map). */
    fun observeAll(): Flow<List<TouristSpotEntity>> = dao.observeAll()

    /** Search by name (reactive). */
    fun searchByName(query: String): Flow<List<TouristSpotEntity>> = dao.searchByName(query)

    /** Get by bounds (one-shot, for large datasets / viewport loads). */
    suspend fun getWithinBounds(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): List<TouristSpotEntity> = withContext(io) {
        dao.getWithinBounds(minLat, maxLat, minLng, maxLng)
    }

    /** Get by id (one-shot). */
    suspend fun getById(id: Long): TouristSpotEntity? = withContext(io) { dao.getById(id) }

    /**
     * Create or update a spot.
     * If `resolveAddress` is true, we mark it for resolution and try to resolve immediately.
     */
    suspend fun saveSpot(
        spot: TouristSpotEntity,
        resolveAddress: Boolean = true
    ): Long = withContext(io) {
        val now = System.currentTimeMillis()
        val toSave = spot.copy(
            updatedAtEpochMs = now,
            // If it is a creation or user edited coordinates/name, trigger address resolution
            needsAddressResolve = resolveAddress || spot.needsAddressResolve
        )
        val id = dao.upsert(toSave)

        if (resolveAddress) {
            tryResolveAddress(id)
        }
        id
    }

    /** Delete spot. */
    suspend fun deleteSpot(spot: TouristSpotEntity): Int = withContext(io) { dao.delete(spot) }

    /** Update only the image URI (handy helper). */
    suspend fun updateImageUri(id: Long, imageUri: String?): Int = withContext(io) {
        val entity = dao.getById(id) ?: return@withContext 0
        dao.update(entity.copy(imageUri = imageUri, updatedAtEpochMs = System.currentTimeMillis()))
    }

    /**
     * Tries to resolve address for a single spot id if it is pending.
     * Best-effort: failure keeps needsAddressResolve = true for future retry.
     */
    suspend fun tryResolveAddress(id: Long): Boolean = withContext(io) {
        val entity = dao.getById(id) ?: return@withContext false
        if (!entity.needsAddressResolve) return@withContext true

        val addr = safeReverseGeocode(entity.lat, entity.lng)
        if (addr != null) {
            dao.setResolvedAddress(id, addr, System.currentTimeMillis())
            true
        } else {
            false
        }
    }

    /**
     * Resolve addresses for all pending spots.
     * Call on app start, settings action, or background work.
     */
    suspend fun resolveAllPendingAddresses(limit: Int = 15): Int = withContext(io) {
        val pending = dao.getPendingAddressResolution()
        var success = 0
        for (spot in pending.take(max(0, limit))) {
            val addr = safeReverseGeocode(spot.lat, spot.lng)
            if (addr != null) {
                dao.setResolvedAddress(spot.id, addr, System.currentTimeMillis())
                success++
            }
        }
        success
    }

    // --- internals ---

    private suspend fun safeReverseGeocode(lat: Double, lng: Double): String? {
        return try {
            geocoding.reverseGeocode(lat, lng)
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            null
        }
    }
}
