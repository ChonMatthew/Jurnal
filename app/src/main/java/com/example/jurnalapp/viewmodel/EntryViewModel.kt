package com.example.jurnalapp.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.jurnalapp.data.EntryDao
import com.example.jurnalapp.data.EntryDatabase
import com.example.jurnalapp.repository.EntryRepository
import com.example.jurnalapp.model.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EntryViewModel(application: Application): AndroidViewModel(application) {

    private val entryDao: EntryDao
    val readAllData: LiveData<List<Entry>>
    private val repository: EntryRepository

    init {
        val entryDatabase = EntryDatabase.getDatabase(application)
        entryDao = entryDatabase.entryDao()
    }

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

    fun addEntryWithImage(entry: Entry, imageBitmap: Bitmap?, context: Context) = viewModelScope.launch {
        val imagePath = if (imageBitmap != null) {
            repository.saveImage(imageBitmap, context)
        } else {
            null
        }
        Log.d("EntryViewModel", "Image Path After Saving: $imagePath") // Check here!
        val entryWithImage = entry.copy(imagePath = imagePath)
        repository.addEntry(entryWithImage)
    }

    fun updateEntryWithImage(entry: Entry, newImageBitmap: Bitmap, context: Context, callback: (Entry) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val newImagePath = if (newImageBitmap != null) {
                repository.saveImage(newImageBitmap, context)
            } else {
                null
            }
            val updatedEntry = entry.copy(imagePath = newImagePath)
            entryDao.updateEntry(updatedEntry)
            Log.d("EntryViewModel", "Old Image Path: ${entry.imagePath}")
            Log.d("EntryViewModel", "New Image Path: $newImagePath")
            withContext(Dispatchers.Main) {
                callback(updatedEntry)
            }
//        val entryWithImage = entry.copy(imagePath = imagePath)
            repository.updateEntry(updatedEntry) // Update the entry
        }
    }

    fun addEntryWithLocation(entry: Entry, imageBitmap: Bitmap?, context: Context, latitude: Double?, longitude: Double?) = viewModelScope.launch {
        val imagePath = if (imageBitmap != null) {
            repository.saveImage(imageBitmap, context)
        } else {
            null
        }
        val entryWithImageAndLocation = entry.copy(imagePath = imagePath, latitude = latitude, longitude = longitude)
        repository.addEntry(entryWithImageAndLocation)
    }

    fun updateEntryWithLocation(entry: Entry, newImageBitmap: Bitmap?, context: Context, latitude: Double?, longitude: Double?, callback: (Entry) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val newImagePath = if (newImageBitmap != null) {
            repository.saveImage(newImageBitmap, context)
        } else {
            null
        }
        val updatedEntry = entry.copy(imagePath = newImagePath, latitude = latitude, longitude = longitude)
        entryDao.updateEntry(updatedEntry)
        withContext(Dispatchers.Main) {
            callback(updatedEntry)
        }
        repository.updateEntry(updatedEntry)
    }
}