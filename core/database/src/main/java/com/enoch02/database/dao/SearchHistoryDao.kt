package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enoch02.database.model.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(value: HistoryItem)

    @Query(value = "SELECT * from items ORDER BY id DESC")
    fun getHistory(): Flow<List<HistoryItem>>

    @Query(value = "DELETE FROM items")
    suspend fun clearAllHistory()
}