package com.aibrowser.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aibrowser.app.domain.Bookmark
import com.aibrowser.app.domain.HistoryEntry
import com.aibrowser.app.domain.AiCache

/**
 * Main application Room database.
 * Holds tables for [Bookmark], [HistoryEntry], and [AiCache].
 */
@Database(entities = [Bookmark::class, HistoryEntry::class, AiCache::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun aiCacheDao(): AiCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "browser.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
