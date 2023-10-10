package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enoch02.database.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert()
    fun insertBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateBook(book: Book)

    @Query(value = "DELETE FROM books WHERE id = :id")
    fun deleteBook(id: Int)

    @Query(value = "DELETE FROM books WHERE id = (:ids)")
    fun deleteBooks(ids: List<Int>)

    @Query(value = "DELETE FROM books")
    fun deleteAll()

    @Query(value = "SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Int): Book

    @Query(value = "SELECT * FROM books")
    fun getBooks(): Flow<List<Book>>
}