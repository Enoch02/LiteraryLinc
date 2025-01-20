package com.enoch02.database

import androidx.room.Room
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: LiteraryLincDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LiteraryLincDatabase::class.java
        ).allowMainThreadQueries().build()
        bookDao = database.getBookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertBook_and_getBookById() = runBlocking {
        val book = Book(id = 1, title = "Test Book", documentMd5 = "md5hash")
        bookDao.insertBook(book)

        val fetchedBook = bookDao.getBookById(1)
        Assert.assertEquals(book, fetchedBook)
    }

    @Test
    fun updateBook_updatesSuccessfully() = runBlocking {
        val book = Book(id = 1, title = "Old Title", documentMd5 = "md5hash")
        bookDao.insertBook(book)

        val updatedBook = book.copy(title = "New Title")
        bookDao.updateBook(updatedBook)

        val fetchedBook = bookDao.getBookById(1)
        Assert.assertEquals("New Title", fetchedBook.title)
    }

    @Test
    fun deleteBook_deletesSuccessfully() = runBlocking {
        val book = Book(id = 1, title = "Test Book", documentMd5 = "md5hash")
        bookDao.insertBook(book)

        bookDao.deleteBook(1)
        val fetchedBook = bookDao.getBookById(1)
        Assert.assertNull(fetchedBook)
    }

    @Test
    fun deleteAll_clearsDatabase() = runBlocking {
        val books = listOf(
            Book(id = 1, title = "Book 1", documentMd5 = "md51"),
            Book(id = 2, title = "Book 2", documentMd5 = "md52")
        )
        books.forEach { bookDao.insertBook(it) }

        bookDao.deleteAll()
        val fetchedBooks = bookDao.getBooksNonFlow()
        Assert.assertTrue(fetchedBooks.isEmpty())
    }

    @Test
    fun getBooks_returnsCorrectList() = runBlocking {
        val books = listOf(
            Book(id = 1, title = "Book 1", documentMd5 = "md51"),
            Book(id = 2, title = "Book 2", documentMd5 = "md52")
        )
        books.forEach { bookDao.insertBook(it) }

        val fetchedBooks = bookDao.getBooksNonFlow()
        Assert.assertEquals(books.size, fetchedBooks.size)
        Assert.assertEquals(books, fetchedBooks)
    }

    @Test
    fun doesBookTitleExist_checksExistenceCorrectly() = runBlocking {
        val book = Book(id = 1, title = "Unique Title", documentMd5 = "md5hash")
        bookDao.insertBook(book)

        val exists = bookDao.doesBookTitleExist("Unique Title").first()
        Assert.assertTrue(exists)

        val doesNotExist = bookDao.doesBookTitleExist("Nonexistent Title").first()
        Assert.assertFalse(doesNotExist)
    }

    @Test
    fun getIdByTitle_returnsCorrectId() = runBlocking {
        val book = Book(id = 1, title = "Test Title", documentMd5 = "md5hash")
        bookDao.insertBook(book)

        val id = bookDao.getIdByTitle("Test Title").first()
        Assert.assertEquals(1, id)
    }

    @Test
    fun doesBookExistByMd5Flow_checksExistenceCorrectly() = runBlocking {
        val book = Book(id = 1, title = "Test Book", documentMd5 = "uniqueMd5")
        bookDao.insertBook(book)

        val exists = bookDao.doesBookExistByMd5Flow("uniqueMd5").first()
        Assert.assertTrue(exists)

        val doesNotExist = bookDao.doesBookExistByMd5Flow("nonexistentMd5").first()
        Assert.assertFalse(doesNotExist)
    }

    @Test
    fun deleteBookWith_deletesSuccessfully() = runBlocking {
        val book = Book(id = 1, title = "Test Book", documentMd5 = "md5hash")
        bookDao.insertBook(book)

        bookDao.deleteBookWith("md5hash")
        val fetchedBook = bookDao.getBookByMd5("md5hash")
        Assert.assertNull(fetchedBook)
    }
}
