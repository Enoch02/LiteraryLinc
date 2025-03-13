package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.enoch02.database.model.Book
import com.enoch02.database.model.Book.Companion.BookStatus
import com.enoch02.database.model.Book.Companion.BookType

@Dao
interface StatsDao {
    @Query(value = "SELECT COUNT (title) FROM books")
    suspend fun getTotalBooks(): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE status = :status")
    suspend fun getCompletedBooks(status: String = BookStatus.COMPLETED.strName): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getMangaCount(type: String = BookType.MANGA.strName): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getLNCount(type: String = BookType.LN.strName): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getComicCount(type: String = BookType.COMIC.strName): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getNovelCount(type: String = BookType.NOVEL.strName): Int

    @Query(value = "SELECT COUNT(*) FROM books WHERE type = :type")
    suspend fun getAnyTypeCount(type: String = BookType.ANY.strName): Int
}