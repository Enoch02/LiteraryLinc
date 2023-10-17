package com.enoch02.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import org.junit.After

import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.jvm.Throws

@RunWith(AndroidJUnit4::class)
class BookDaoTest {
    private lateinit var bookDao: BookDao
    private lateinit var db: LiteraryLincDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LiteraryLincDatabase::class.java).build()
        bookDao = db.getBookDao()
    }

    @Test
    @Throws(Exception::class)
    fun addBookAndReadBook() {
        val book = Book(id = 13, title = "Test", type = BookType.BOOK)
        bookDao.insertBook(book)
        val bookFromDb = bookDao.getBookById(13)

        assertEquals(book.id, bookFromDb.id)
    }

    @Test
    @Throws(Exception::class)
    fun updateBook() {
        val newBook = Book(id = 13, title = "Test(Updated)", type = BookType.BOOK)
        bookDao.updateBook(newBook)
        val bookFromDb = bookDao.getBookById(13)

        assertEquals("Test(Updated)", bookFromDb.title)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}