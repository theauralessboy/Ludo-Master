package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameStatEntity::class], version = 1, exportSchema = false)
abstract class LudoDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao

    companion object {
        @Volatile
        private var INSTANCE: LudoDatabase? = null

        fun getDatabase(context: Context): LudoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LudoDatabase::class.java,
                    "ludo_master_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
