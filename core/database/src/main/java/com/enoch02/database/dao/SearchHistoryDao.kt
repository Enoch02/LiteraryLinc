package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.enoch02.database.model.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert
    suspend fun insertQuery(value: HistoryItem)

    @Query(value = "SELECT * from items")
    fun getHistory(): Flow<List<HistoryItem>>

    @Query(value = "DELETE FROM items")
    suspend fun clearAllHistory()
}