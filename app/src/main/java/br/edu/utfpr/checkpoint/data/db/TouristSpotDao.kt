package br.edu.utfpr.checkpoint.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.edu.checkpoint.checkpoint.data.model.TouristSpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TouristSpotDao {

    /** Reactive list for UI (list/map) */
    @Query("SELECT * FROM tourist_spots ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<TouristSpotEntity>>

    /** Non-reactive load (useful for one-shot jobs) */
    @Query("SELECT * FROM tourist_spots ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<TouristSpotEntity>

    @Query("SELECT * FROM tourist_spots WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TouristSpotEntity?

    /** Case-insensitive substring search by name */
    @Query("""
        SELECT * FROM tourist_spots
        WHERE name LIKE '%' || :q || '%'
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun searchByName(q: String): Flow<List<TouristSpotEntity>>

    /** Bounding box query for map viewport */
    @Query("""
        SELECT * FROM tourist_spots
        WHERE lat BETWEEN :minLat AND :maxLat
          AND lng BETWEEN :minLng AND :maxLng
        ORDER BY name COLLATE NOCASE ASC
    """)
    suspend fun getWithinBounds(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): List<TouristSpotEntity>

    /** Spots with pending address resolution */
    @Query("SELECT * FROM tourist_spots WHERE needsAddressResolve = 1")
    suspend fun getPendingAddressResolution(): List<TouristSpotEntity>

    /** Light partial updates for address resolution */
    @Query("""
        UPDATE tourist_spots
        SET address = :address,
            updatedAtEpochMs = :updatedAt,
            needsAddressResolve = 0
        WHERE id = :id
    """)
    suspend fun setResolvedAddress(id: Long, address: String?, updatedAt: Long = System.currentTimeMillis())

    /** Standard CRUD */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TouristSpotEntity): Long

    @Update
    suspend fun update(entity: TouristSpotEntity): Int

    @Delete
    suspend fun delete(entity: TouristSpotEntity): Int

    /** Convenient upsert (Room 2.6 tem @Upsert; alternativa manual abaixo) */
    @Transaction
    suspend fun upsert(entity: TouristSpotEntity): Long {
        return if (entity.id == 0L) {
            insert(entity)
        } else {
            update(entity)
            entity.id
        }
    }

    /** Update only the updatedAt (call when editing without touching address) */
    @Query("UPDATE tourist_spots SET updatedAtEpochMs = :updatedAt WHERE id = :id")
    suspend fun touch(id: Long, updatedAt: Long = System.currentTimeMillis())
}
