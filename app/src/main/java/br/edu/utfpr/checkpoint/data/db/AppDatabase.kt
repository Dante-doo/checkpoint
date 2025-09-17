package br.edu.utfpr.checkpoint.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.edu.checkpoint.checkpoint.data.model.TouristSpotEntity

/**
 * Room database holding tourist spots.
 * Schema version 1 (add migrations in future versions).
 */
@Database(
    entities = [TouristSpotEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    // AppConverters::class // enable if you switch to Uri/Instant types
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun touristSpotDao(): TouristSpotDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "checkpoint.db"
                )
                    // For dev only; prefer proper migrations in production
                    //.fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
