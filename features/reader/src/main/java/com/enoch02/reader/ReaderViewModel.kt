package com.enoch02.reader

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.LLDocument
import com.enoch02.database.model.ReaderSorting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.min


@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val documentDao: DocumentDao,
    private val bookDao: BookDao,
    bookCoverRepository: BookCoverRepository
) :
    ViewModel() {
    val covers = bookCoverRepository.latestCoverPath

    /**
     * Retrieve (if any) documents from the database
     *
     * @param sorting how should the documents be sorted?
     * @return a list of the documents in a flow
     * */
    fun getDocuments(sorting: ReaderSorting): Flow<List<LLDocument>> {
        return when (sorting) {
            ReaderSorting.NAME -> {
                documentDao.getDocumentsByName().map {
                    it.sortedWith { a, b -> a.name.naturalCompare(b.name) }
                }
            }

            ReaderSorting.LAST_READ -> {
                documentDao.getDocumentsByLastRead()
            }

            ReaderSorting.SIZE -> {
                documentDao.getDocumentsBySize()
            }

            ReaderSorting.FORMAT -> {
                documentDao.getDocumentsByFormat()
            }
        }
    }

    /**
     * Update stored document info after the user has finished reading
     *
     * @param document the document to update
     */
    fun updateDocumentInfo(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.updateDocument(document)
            updateBookListEntry(document)
        }
    }

    /**
     * Mark document as read or unread
     *
     * @param document document to change its read status
     * */
    fun toggleDocumentReadStatus(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.updateDocument(document.copy(isRead = !document.isRead))

            if (bookDao.isDocumentInBooklist(document.id)) {
                val temp = bookDao.getBookByMd5(document.id)
                temp?.let { theBook ->
                    bookDao.updateBook(
                        theBook.copy(
                            dateCompleted = getCurrentTime(),
                            pagesRead = document.pages,
                            // just in case..
                            pageCount = document.pages
                        )
                    )
                }
            }
        }
    }

    /**
     * Add or remove a document from favorites
     *
     * @param document document to change its favorite status
     * */
    fun toggleFavoriteStatus(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.updateDocument(document.copy(isFavorite = !document.isFavorite))
        }
    }

    /**
     * Add a new document to the book list for `tracking`
     *
     * @param document The document to add to the book list database
     */
    fun createBookListEntry(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!bookDao.isDocumentInBooklist(document.id)) {
                val now = getCurrentTime()
                val pagesRead = if (document.currentPage < 0) {
                    0
                } else {
                    document.currentPage
                }

                val newBook = Book(
                    title = document.name,
                    author = document.author,
                    pagesRead = pagesRead,
                    pageCount = document.pages,
                    dateStarted = now,
                    documentMd5 = document.id,
                    coverImageName = document.cover,
                )

                bookDao.insertBook(newBook)
            }
        }
    }

    /**
     * Remove a document from the book list database
     *
     * @param documentId the md5 of the document
     * */
    fun removeBookListEntry(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (bookDao.isDocumentInBooklist(documentId)) {
                bookDao.deleteBookWith(documentId)
            }
        }
    }

    private fun updateBookListEntry(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = bookDao.getBookByMd5(document.id)

            book?.let { theBook ->
                bookDao.updateBook(
                    theBook.copy(
                        pageCount = document.pages,
                        pagesRead = document.currentPage,
                        coverImageName = if (theBook.coverImageName.isNullOrEmpty()) document.cover else theBook.coverImageName
                    )
                )
            }
        }
    }

    /**
     * Check if a document has been added to the book list database
     *
     * @param documentId id to check
     * */
    fun isDocumentInBookList(documentId: String): Flow<Boolean> {
        return bookDao.checkDocument(documentId)
    }

    private fun getCurrentTime(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().toEpochMilli()
        } else {
            Calendar.getInstance().time.time
        }
    }
}

/**
 * Compare strings the way God intended, "Item 2" should come before "Item 12"
 *
 * @param other string being compared with [this]
 * @return an int used for comparison in a comparator
 * */
fun String.naturalCompare(other: String): Int {
    val splitPattern = Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")
    val thisParts = this.split(splitPattern)
    val otherParts = other.split(splitPattern)

    val minLength = min(thisParts.size, otherParts.size)

    for (i in 0 until minLength) {
        val thisPart = thisParts[i]
        val otherPart = otherParts[i]

        val comparison = if (thisPart.first().isDigit() && otherPart.first().isDigit()) {
            // If both parts are numeric, compare them as integers
            thisPart.toIntOrNull()?.compareTo(otherPart.toIntOrNull() ?: 0) ?: 0
        } else {
            // Otherwise, compare them as strings
            thisPart.compareTo(otherPart)
        }

        if (comparison != 0) return comparison
    }

    // If all compared parts are equal, compare by length
    return thisParts.size.compareTo(otherParts.size)
}
