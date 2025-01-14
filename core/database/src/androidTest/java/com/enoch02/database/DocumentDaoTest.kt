package com.enoch02.database

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.LLDocument
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.Date

@RunWith(AndroidJUnit4::class)
class DocumentDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DocumentDatabase
    private lateinit var documentDao: DocumentDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DocumentDatabase::class.java
        ).allowMainThreadQueries().build()

        documentDao = database.getDocumentDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertDocuments_insertAndRetrieveDocument() = runBlocking {
        val document = LLDocument(
            id = "1",
            contentUri = Uri.EMPTY,
            name = "Sample Doc",
            lastRead = Date.from(Instant.now()),
            type = "PDF"
        )
        documentDao.insertDocuments(listOf(document))

        val retrievedDocument = documentDao.getDocument("1")
        assertNotNull(retrievedDocument)
        assertEquals("Sample Doc", retrievedDocument?.name)
    }

    @Test
    fun retrieveAllDocumentsFlow_retrievesSuccessfully() = runBlocking {
        val documents = listOf(
            LLDocument(
                id = "1",
                contentUri = Uri.parse("uri://example1"),
                name = "Doc 1",
                lastRead = Date.from(Instant.now()),
                type = "PDF"
            ),
            LLDocument(
                id = "2",
                contentUri = Uri.parse("uri://example2"),
                name = "Doc 2",
                lastRead = Date.from(Instant.now()),
                type = "PDF"
            )
        )
        documentDao.insertDocuments(documents)

        val retrievedDocuments = documentDao.getDocuments().first()
        assertEquals(2, retrievedDocuments.size)
    }

    @Test
    fun retrieveAllDocumentsNonFlow_retrievesSuccessfully() = runBlocking {
        val documents = listOf(
            LLDocument(
                id = "1",
                contentUri = Uri.parse("uri://example1"),
                name = "Doc 1",
                lastRead = Date.from(Instant.now()),
                type = "PDF"
            ),
            LLDocument(
                id = "2",
                contentUri = Uri.parse("uri://example2"),
                name = "Doc 2",
                lastRead = Date.from(Instant.now()),
                type = "PDF"
            )
        )
        documentDao.insertDocuments(documents)

        val retrievedDocuments = documentDao.getDocumentsNonFlow()
        assertEquals(2, retrievedDocuments.size)
    }

    @Test
    fun updateDocument_updatesSuccessfully() = runBlocking {
        val document =
            LLDocument(
                id = "1",
                contentUri = Uri.parse("uri://example"),
                name = "Original Title",
                lastRead = Date.from(Instant.now()),
                type = "PDF"
            )
        documentDao.insertDocuments(listOf(document))

        val updatedDocument = document.copy(name = "Updated Title")
        documentDao.updateDocument(updatedDocument)

        val retrievedDocument = documentDao.getDocument("1")
        assertNotNull(retrievedDocument)
        assertEquals("Updated Title", retrievedDocument?.name)
    }

    @Test
    fun deleteDocument_deletesSuccessfully() = runBlocking {
        val document = LLDocument(
            id = "1",
            contentUri = Uri.parse("uri://example"),
            name = "Sample Doc",
            lastRead = Date.from(Instant.now()),
            type = "EPUB"
        )
        documentDao.insertDocuments(listOf(document))

        documentDao.deleteDocument("uri://example")
        val retrievedDocument = documentDao.getDocument("1")
        assertNull(retrievedDocument)
    }

    @Test
    fun getDocumentCount_countIsCorrect() = runBlocking {
        val documents = listOf(
            LLDocument(
                id = "1",
                contentUri = Uri.parse("uri://example1"),
                name = "Doc 1",
                lastRead = Date.from(Instant.now()),
                type = "PDF"
            ),
            LLDocument(
                id = "2",
                contentUri = Uri.parse("uri://example2"),
                name = "Doc 2",
                lastRead = Date.from(Instant.now()),
                type = "PDF"
            )
        )
        documentDao.insertDocuments(documents)

        val count = documentDao.getDocumentCount().first()
        assertEquals(2, count)
    }
}
