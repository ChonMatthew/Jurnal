package com.example.jurnalapp.data.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jurnalapp.data.EntryDatabase
import com.example.jurnalapp.data.model.Entry
import com.example.jurnalapp.data.repository.EntryRepository
import kotlinx.coroutines.launch

class AddEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EntryRepository

    init {
        val entryDao = EntryDatabase.getDatabase(application).entryDao()
        repository = EntryRepository(entryDao)
    }

    fun insertDataToDatabase(
        title: String,
        subtitle: String,
        content: String,
        dateInMillis: Long,
        timeInMillis: Long,
        imageBitmap: Bitmap?,
        context: Context,
        callback: (Boolean) -> Unit
    ) {
        if (inputCheck(title, subtitle, content)) {
            val entry = Entry(0, title, subtitle, content, dateInMillis, timeInMillis, null)
            viewModelScope.launch {
                if (imageBitmap != null) {
                    val imagePath = repository.saveImage(imageBitmap, context)
                    val entryWithImage = entry.copy(imagePath = imagePath)
                    repository.addEntry(entryWithImage)
                } else {
                    repository.addEntry(entry)
                }
                callback(true)
            }
        } else {
            callback(false)
        }
    }

    private fun inputCheck(title: String, subtitle: String, content: String): Boolean {
        return !(title.isEmpty() || subtitle.isEmpty() || content.isEmpty())
    }
}
