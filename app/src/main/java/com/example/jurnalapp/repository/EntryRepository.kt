package com.example.jurnalapp.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.LiveData
import com.example.jurnalapp.data.EntryDao
import com.example.jurnalapp.model.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    suspend fun saveImage(imageBitmap: Bitmap, context: Context): String? {
        return withContext(Dispatchers.IO) {
            val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File(imagesDir, "image_${System.currentTimeMillis()}.jpg")
            try {
            FileOutputStream(imageFile).use { outputStream ->
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }
            imageFile.absolutePath // Return the absolute file path
        } catch (e: Exception) {
            // Handle exceptions
            null
        }
        }
    }
}