package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.enoch02.database.model.Book

@Dao
interface StatsDao {
    @Query(value = "SELECT COUNT (title) FROM books")
    suspend fun getTotalBooks(): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE status = :status")
    suspend fun getCompletedBooks(status: String = Book.status[1]): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getMangaCount(type: String = Book.types[3].toString()): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getLNCount(type: String = Book.types[2].toString()): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getComicCount(type: String = Book.types[1].toString()): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getNovelCount(type: String = Book.types[4].toString()): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getAnyTypeCount(type: String = Book.types[0].toString()): Int
}