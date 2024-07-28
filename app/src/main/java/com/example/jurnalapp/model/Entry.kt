package com.example.jurnalapp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "entry_table")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val subtitle: String,
    val content: String,
    val date: Long,
    val time: Long
): Parcelable