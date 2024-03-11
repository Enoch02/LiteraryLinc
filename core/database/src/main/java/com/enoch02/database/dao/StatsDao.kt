package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.enoch02.database.model.Book

@Dao
interface StatsDao {
    @Query(value = "SELECT COUNT (title) FROM books")
    suspend fun getTotalBooks(): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE status = :status")
    suspend fun getCompletedBooks(status: String=Book.status[1]): Int

    /*suspend fun getMangaCount(): Int

    suspend fun getLNCount(): Int

    suspend fun getComicCount(): Int

    suspend fun getNovelCount(): Int

    suspend fun getAnyTypeCount(): Int*/
}