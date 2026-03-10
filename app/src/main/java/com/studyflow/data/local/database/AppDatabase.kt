package com.studyflow.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.studyflow.data.local.dao.DeckDao
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.local.entity.SessionResultEntity

@Database(
    entities = [DeckEntity::class, CardEntity::class, SessionResultEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flashcard_db"
                )
                .fallbackToDestructiveMigration() // Simple for dev, use migration in prod
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
