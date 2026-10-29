package com.example.bibliotecnm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PrestamoPendiente::class], version = 3, exportSchema = false)
abstract class DataBaseLocal : RoomDatabase() {
    abstract fun prestamoPendienteDao(): PrestamoPendienteDao

    companion object {
        @Volatile
        private var INSTANCE: DataBaseLocal? = null
        fun getDatabase(context: Context): DataBaseLocal {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBaseLocal::class.java,
                    "biblioteca_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}