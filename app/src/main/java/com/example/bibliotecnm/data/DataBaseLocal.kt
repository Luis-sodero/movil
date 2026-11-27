package com.example.bibliotecnm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PrestamoPendiente::class, LibroGuardado::class], version = 4, exportSchema = false)
abstract class DataBaseLocal : RoomDatabase() {
    abstract fun prestamoPendienteDao(): PrestamoPendienteDao
    abstract fun libroDao(): LibroDao

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