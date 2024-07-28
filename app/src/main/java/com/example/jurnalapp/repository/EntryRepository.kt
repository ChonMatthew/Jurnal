package com.example.jurnalapp.repository

import androidx.lifecycle.LiveData
import com.example.jurnalapp.data.EntryDao
import com.example.jurnalapp.model.Entry
import kotlinx.coroutines.flow.Flow

class EntryRepository(private val entryDao: EntryDao) {

    val readAllData: LiveData<List<Entry>> = entryDao.readAllData()

    suspend fun addEntry(entry: Entry) {
        entryDao.addEntry(entry)

    }

    suspend fun updateEntry(entry: Entry) {
        entryDao.updateEntry(entry)

    }

    suspend fun deleteEntry(entry: Entry) {
        entryDao.deleteEntry(entry)
    }

    suspend fun deleteAllEntries() {
        entryDao.deleteAllEntries()
    }

    fun searchDatabase(searchQuery: String): Flow<List<Entry>> {
        return entryDao.searchDatabase(searchQuery)
    }
}