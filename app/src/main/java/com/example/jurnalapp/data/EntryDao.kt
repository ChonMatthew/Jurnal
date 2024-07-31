package com.example.jurnalapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jurnalapp.data.model.Entry
import kotlinx.coroutines.flow.Flow

// Data Access Objects for the Entry entity
@Dao
interface EntryDao {
    // Inserts a new entry
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEntry(entry: Entry)

    // Retrieves all entries from the database
    @Query("SELECT * FROM entry_table ORDER BY id DESC")
    fun readAllData(): LiveData<List<Entry>>

    // Updates the database with the new updated entry
    @Update
    suspend fun updateEntry(entry: Entry)

    // Deletes an entry from the database
    @Delete
    suspend fun deleteEntry(entry: Entry)

    // Delete all entries from the database
    @Query("DELETE FROM entry_table")
    suspend fun deleteAllEntries()

    // Query used for searching entries by title or subtitle
    @Query("SELECT * FROM entry_table WHERE title LIKE :searchQuery OR subtitle LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<List<Entry>>
}