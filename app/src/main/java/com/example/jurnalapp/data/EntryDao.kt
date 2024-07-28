package com.example.jurnalapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jurnalapp.model.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEntry(entry: Entry)

    @Query("SELECT * FROM entry_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Entry>>

    @Update
    suspend fun updateEntry(entry: Entry)

    @Delete
    suspend fun deleteEntry(entry: Entry)

    @Query("DELETE FROM entry_table")
    suspend fun deleteAllEntries()

    @Query("SELECT * FROM entry_table WHERE title LIKE :searchQuery OR subtitle LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<List<Entry>>
}