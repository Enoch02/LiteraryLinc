package com.enoch02.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class HistoryItem(
    @PrimaryKey val id: Int? = null,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)