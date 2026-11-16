package com.example.uniprioritizer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskDraftEntity::class], version = 1, exportSchema = true)
abstract class UniPrioritizerDatabase : RoomDatabase() {
    abstract fun taskDraftDao(): TaskDraftDao

    companion object {
        private const val DATABASE_NAME = "uniprioritizer.db"

        fun create(context: Context): UniPrioritizerDatabase =
            Room.databaseBuilder(context, UniPrioritizerDatabase::class.java, DATABASE_NAME).build()
    }
}
