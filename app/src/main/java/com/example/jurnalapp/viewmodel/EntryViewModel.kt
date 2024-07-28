package com.example.jurnalapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.jurnalapp.data.EntryDatabase
import com.example.jurnalapp.repository.EntryRepository
import com.example.jurnalapp.model.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EntryViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<Entry>>
    private val repository: EntryRepository

    init {
        val entryDao = EntryDatabase.getDatabase(application).entryDao()
        repository = EntryRepository(entryDao)
        readAllData = repository.readAllData
    }

    fun addEntry(entry: Entry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEntry(entry)
        }
    }

    fun updateEntry(entry: Entry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEntry(entry)
        }
    }

    fun deleteEntry(entry: Entry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEntry(entry)
        }
    }

    fun deleteAllEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllEntries()
        }
    }

    fun searchDatabase(searchQuery: String): LiveData<List<Entry>> {
        return repository.searchDatabase(searchQuery).asLiveData()
    }

    fun updateEntryWithDateTime(entry: Entry, date: Long, time: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedEntry = entry.copy(date = date, time = time)
            repository.updateEntry(updatedEntry)
        }
    }
}