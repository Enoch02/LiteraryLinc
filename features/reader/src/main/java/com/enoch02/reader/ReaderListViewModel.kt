package com.enoch02.reader

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.LLDocument
import com.enoch02.database.model.ReaderFilter
import com.enoch02.database.model.ReaderSorting
import com.enoch02.database.model.deleteDocument
import com.enoch02.database.model.existsAsFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.min


@HiltViewModel
class ReaderListViewModel @Inject constructor(
    private val applicationContext: Context,
    private val documentDao: DocumentDao,
    private val bookDao: BookDao,
    bookCoverRepository: BookCoverRepository
) :
    ViewModel() {
    val covers = bookCoverRepository.latestCoverPath
    private val documents = documentDao.getDocuments()
    private val _sorting = MutableStateFlow(ReaderSorting.LAST_READ)
    private val _filter = MutableStateFlow(ReaderFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val documentsState: StateFlow<DocumentsState> = combine(_sorting, _filter) { sorting, filter ->
        getDocuments(sorting, filter)
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 5 second timeout
            initialValue = DocumentsState.Loading
        )


    /**
     * Retrieve (if any) documents from the database
     *
     * @param sorting how should the documents be sorted?
     * @param filter  what type of documents should be returned?
     * @return a list of the documents in a flow
     * */
    private fun getDocuments(sorting: ReaderSorting, filter: ReaderFilter): Flow<DocumentsState> {
        return flow {
            emit(DocumentsState.Loading)
            documents.collect { documents ->
                emit(
                    DocumentsState.Loaded(
                        documents
                            .fastFilter { filterPredicate(applicationContext, filter, it) }
                            .sortedWith(sortingComparator(sorting))
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun updateSorting(sorting: ReaderSorting) {
        _sorting.value = sorting
    }

    fun updateFilter(filter: ReaderFilter) {
        _filter.value = filter
    }

    private fun filterPredicate(
        context: Context,
        filter: ReaderFilter,
        document: LLDocument
    ): Boolean {
        return when (filter) {
            ReaderFilter.READING -> document.currentPage > 0 && document.currentPage < document.pages && !document.isRead && document.existsAsFile(
                context
            )

            ReaderFilter.FAVORITES -> document.isFavorite
            ReaderFilter.COMPLETED -> document.isRead
            ReaderFilter.ALL -> true
            ReaderFilter.NO_FILE -> !document.existsAsFile(context)
        }
    }

    private fun sortingComparator(sorting: ReaderSorting): Comparator<LLDocument> {
        return when (sorting) {
            ReaderSorting.NAME -> Comparator { a, b -> a.name.naturalCompare(b.name) }
            ReaderSorting.LAST_READ -> compareByDescending(LLDocument::lastRead)
            ReaderSorting.SIZE -> compareByDescending(LLDocument::sizeInMb)
            ReaderSorting.FORMAT -> compareBy(LLDocument::type)
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

            if (bookDao.doesBookExistByMd5(document.id)) {
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
            if (!bookDao.doesBookExistByMd5(document.id) && document.autoTrackable) {
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
            if (bookDao.doesBookExistByMd5(documentId)) {
                bookDao.deleteBookWith(documentId)
            }
        }
    }

    /**
     * Check if a document has been added to the book list database
     *
     * @param documentId id to check
     * */
    fun isDocumentInBookList(documentId: String): Flow<Boolean> {
        return bookDao.doesBookExistByMd5Flow(documentId)
    }

    private fun getCurrentTime(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().toEpochMilli()
        } else {
            Calendar.getInstance().time.time
        }
    }

    fun toggleDocumentAutoTracking(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.updateDocument(document.copy(autoTrackable = !document.autoTrackable))
        }
    }

    fun deleteDocument(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            if (document.deleteDocument(applicationContext)) {
                documentDao.deleteDocument(document.contentUri.toString())
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Document could not be deleted",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    fun searchFor(text: String): Flow<List<LLDocument>> {
        return documents.map { documents ->
            if (text.isBlank()) {
                emptyList()
            } else {
                documents.filter {
                    it.name.contains(text, ignoreCase = true) ||
                            it.author.contains(text, ignoreCase = true)
                }
            }
        }
    }
}

sealed class DocumentsState {
    data object Loading : DocumentsState()
    data class Loaded(val documents: List<LLDocument>) : DocumentsState()
}

/**
 * Compare strings the way God intended, "Item 2" should come before "Item 12"
 *
 * @param other string being compared with [this]
 * @return an int used for comparison in a comparator
 * */
fun String.naturalCompare(other: String): Int {
    val splitPattern = Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")
    val thisParts = this.lowercase().split(splitPattern)
    val otherParts = other.lowercase().split(splitPattern)

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
