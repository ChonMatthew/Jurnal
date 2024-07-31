package com.example.jurnalapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.jurnalapp.data.model.Entry

// Database class for the entity
@Database(entities = [Entry::class], version = 1, exportSchema = false)
abstract class EntryDatabase: RoomDatabase() {

    // Abstract function to get the DAO
    abstract fun entryDao(): EntryDao

    // Companion object to provide a singleton instance of the database
    companion object{
        @Volatile
        private var INSTANCE: EntryDatabase? = null

        // Function to get the database instance
        fun getDatabase(context: Context): EntryDatabase {
            // Check if an instance already exists
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            // If the instance is null, create a new database instance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EntryDatabase::class.java,
                    "entry_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}