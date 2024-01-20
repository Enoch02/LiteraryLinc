package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enoch02.database.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert()
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Query(value = "DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: Int)

    @Query(value = "DELETE FROM books WHERE id = (:ids)")
    suspend fun deleteBooks(ids: List<Int>)

    @Query(value = "DELETE FROM books")
    suspend fun deleteAll()

    @Query(value = "SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Int): Book

    @Query(value = "SELECT * FROM books")
    fun getBooks(): Flow<List<Book>>

    @Query(value = "SELECT * FROM books")
    suspend fun getBooksNonFlow(): List<Book>

    @Query(value = "SELECT EXISTS (SELECT 1 FROM books WHERE title = :bookTitle)")
    fun checkBookTitle(bookTitle: String): Flow<Boolean>

    @Query(value = "SELECT id FROM books WHERE title = :bookTitle LIMIT 1")
    fun getIdByTitle(bookTitle: String): Flow<Int>
}