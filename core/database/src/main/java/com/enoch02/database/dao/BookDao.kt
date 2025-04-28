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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query(value = "SELECT * FROM books WHERE id = :id")
    fun getBookByIdFlow(id: Int): Flow<Book?>

    @Query(value = "SELECT * FROM books")
    fun getBooks(): Flow<List<Book>>

    @Query(value = "SELECT * FROM books")
    suspend fun getBooksNonFlow(): List<Book>

    @Query(value = "SELECT EXISTS (SELECT 1 FROM books WHERE title = :bookTitle LIMIT 1)")
    fun doesBookTitleExist(bookTitle: String): Flow<Boolean>

    @Query(value = "SELECT id FROM books WHERE title = :bookTitle LIMIT 1")
    fun getIdByTitle(bookTitle: String): Flow<Int>

    @Query(value = "SELECT EXISTS (SELECT 1 FROM books WHERE documentMd5 = :md5 LIMIT 1)")
    fun doesBookExistByMd5Flow(md5: String): Flow<Boolean>

    @Query(value = "SELECT EXISTS (SELECT 1 FROM books WHERE documentMd5 = :md5 LIMIT 1)")
    suspend fun doesBookExistByMd5(md5: String): Boolean

    @Query(value = "DELETE FROM books WHERE documentMd5 = :md5")
    suspend fun deleteBookWith(md5: String)

    @Query(value = "SELECT * FROM books WHERE documentMd5 = :documentMd5")
    suspend fun getBookByMd5(documentMd5: String): Book?
}