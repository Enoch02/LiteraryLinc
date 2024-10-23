package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.enoch02.database.model.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert
    suspend fun insertQuery(item: HistoryItem)

    @Update
    suspend fun updateQuery(item: HistoryItem)

    @Query(value = "SELECT * from items ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<HistoryItem>>

    @Query(value = "SELECT EXISTS (SELECT 1 FROM items WHERE value =:value)")
    suspend fun checkItemValue(value: String): Boolean

    @Query(value = "DELETE FROM items")
    suspend fun clearAllHistory()

    @Query(value = "DELETE FROM items WHERE value =:value")
    suspend fun delete(value: String)
}