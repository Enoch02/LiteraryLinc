package com.enoch02.viewer

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.Link
import com.artifex.mupdf.fitz.Outline
import com.artifex.mupdf.fitz.Page
import com.artifex.mupdf.fitz.android.AndroidDrawDevice
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Book.Companion.BookStatus
import com.enoch02.database.model.LLDocument
import com.enoch02.resources.BitmapManager
import com.enoch02.resources.mupdf.ContentInputStream
import com.enoch02.settings.SettingsRepository
import com.enoch02.viewer.model.ContentState
import com.enoch02.resources.mupdf.model.Item
import com.enoch02.resources.mupdf.model.LinkItem
import com.enoch02.resources.mupdf.model.SearchResult
import com.enoch02.settings.ReadingProgressManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Instant
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

private const val TAG = "LLDocumentView"

@HiltViewModel
class LLDocumentViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val documentDao: DocumentDao,
    private val settingsRepository: SettingsRepository,
    private val bitmapManager: BitmapManager,
    val readingProgressManager: ReadingProgressManager
) : ViewModel() {
    var contentState by mutableStateOf(ContentState.LOADING)
    var document by mutableStateOf<Document?>(null)
    var currentPage by mutableIntStateOf(0)
    val pages = mutableStateListOf<Page>()
    var hasOutline by mutableStateOf(false)
    val flatOutline = mutableStateListOf<Item>()
    var searchQuery by mutableStateOf("")
    var searchResults by mutableStateOf(emptyList<SearchResult>())
    var showSearchResults by mutableStateOf(false)
    var searchInProgress by mutableStateOf(false)
    var documentLinks by mutableStateOf(emptyList<LinkItem>())
    var showLinks by mutableStateOf(false)

    var requiresPassword by mutableStateOf(false)
    var password by mutableStateOf("")
    private val _visitedPages = mutableStateOf<List<Int>>(emptyList()) // Stack as a list
    val visitedPages by _visitedPages
    private var _documentInfo = mutableStateOf(DocumentInfo())
    val documentInfo by _documentInfo
    private var _showRereadDialog = mutableStateOf(false)
    val showRereadDialog by _showRereadDialog

    private var docTitle by mutableStateOf("")
    private var docKey = ""
    private var size: Long = -1

    private var documentPageCount = 0
    private var documentId: String? = null

    private val pageLock = Any()
    private val updateJobMutex = Mutex()
    private val documentAccessLock = Any()

    private var deviceWidth = 0

    val dynamicColor = getPreference(key = SettingsRepository.BooleanPreferenceType.DYNAMIC_COLOR)
    var showBarsInit = false

    fun initDocument(context: Context, uri: Uri, mimeType: String?, id: String?) {
        var cursor: Cursor? = null

        docKey = uri.toString()
        documentId = id
        deviceWidth = context.resources.displayMetrics.widthPixels
        viewModelScope.launch(Dispatchers.IO) {
            showBarsInit =
                getPreference(key = SettingsRepository.BooleanPreferenceType.SHOW_DOC_VIEWER_BARS).first()
        }

        try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                var idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.getType(idx) == Cursor.FIELD_TYPE_STRING)
                    docTitle = cursor.getString(idx)

                idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && cursor.getType(idx) == Cursor.FIELD_TYPE_INTEGER)
                    size = cursor.getLong(idx)

                if (size == 0L) size = -1
            }
        } catch (x: Exception) {
            // Ignore any exception and depend on default values for title
            // and size (unless one was decoded)
        } finally {
            cursor?.close()
        }

        if (mimeType == null || mimeType == "application/octet-stream") {
            openDocument(context, uri, size, context.contentResolver.getType(uri) ?: docTitle)
        } else {
            openDocument(context, uri, size, mimeType)
        }
    }

    @Throws(IOException::class)
    private fun openDocument(context: Context, uri: Uri, size: Long, mimetype: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cr: ContentResolver = context.contentResolver
                val inputStream = cr.openInputStream(uri)
                var buf: ByteArray? = null
                var used = -1
                try {
                    val limit = 8 * 1024 * 1024
                    if (size < 0) { // size is unknown
                        buf = ByteArray(limit)
                        used = inputStream!!.read(buf)
                        val atEOF = inputStream.read() == -1
                        if (used < 0 || (used == limit && !atEOF))  // no or partial data
                            buf = null
                    } else if (size <= limit) { // size is known and below limit
                        buf = ByteArray(size.toInt())
                        used = inputStream!!.read(buf)
                        if (used < 0 || used < size)  // no or partial data
                            buf = null
                    }
                    if (buf != null && buf.size != used) {
                        val newbuf = ByteArray(used)
                        System.arraycopy(buf, 0, newbuf, 0, used)
                        buf = newbuf
                    }
                } catch (e: OutOfMemoryError) {
                    buf = null
                } finally {
                    inputStream!!.close()
                }

                if (buf != null) {
                    Log.i(TAG, "  Opening document from memory buffer of size " + buf.size)
                    document = Document.openDocument(buf, mimetype)
                } else {
                    Log.i(TAG, "  Opening document from stream")
                    document = Document.openDocument(
                        ContentInputStream(
                            cr,
                            uri,
                            size
                        ), mimetype
                    )
                }

                if (document!!.needsPassword()) {
                    requiresPassword = true
                } else {
                    documentPageCount = document!!.countPages()
                    getCurrentPage()
                    loadOutline()
                    loadPages()
                    getDocumentLinks()
                    _documentInfo.value = DocumentInfo(
                        document!!.getMetaData(Document.META_INFO_TITLE),
                        document!!.getMetaData(Document.META_INFO_AUTHOR),
                        document!!.getMetaData(Document.META_INFO_SUBJECT),
                        document!!.getMetaData(Document.META_INFO_CREATIONDATE),
                        document!!.getMetaData(Document.META_INFO_MODIFICATIONDATE),
                        document!!.getMetaData(Document.META_INFO_CREATOR),
                        document!!.getMetaData(Document.META_INFO_PRODUCER),
                        document!!.getMetaData(Document.META_INFO_KEYWORDS),
                        document!!.getMetaData(Document.META_ENCRYPTION),
                    )
                    contentState = ContentState.NOT_LOADING

                    hasBookBeenCompleted()
                }
            } catch (e: Exception) {
                // catch exceptions that occurs when user closes the screen
                // before a document loads
                contentState = ContentState.DOCUMENT_NOT_FOUND
                Log.e(TAG, "openDocument: ${e.message}")
            }
        }
    }

    private fun loadPages() {
        try {
            if (document != null) {
                for (i in 0..<documentPageCount) {
                    val page = document!!.loadPage(i)
                    pages.add(page)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadPages: ${e.message}")
        }
    }

    fun getPageBitmap(index: Int, zoom: Float): Flow<Bitmap?> = flow {
        val pageKey = "${docTitle}-$index-z=$zoom"
        Log.d(TAG, "getPageBitmap: loading page $pageKey")
        if (document != null) {
            val cachedBitmap = bitmapManager.getCachedBitmap(pageKey)

            if (cachedBitmap != null && !cachedBitmap.isRecycled) {
                Log.i(TAG, "getPageBitmap: using cached bitmap!")
                emit(cachedBitmap)

            } else {
                Log.i(TAG, "getPageBitmap: page not in cache")
                bitmapManager.releaseBitmap(cachedBitmap)
                try {
                    val page: Page = pages[index]
                    val ctm = AndroidDrawDevice.fitPageWidth(page, deviceWidth)
                    if (zoom != 1f) {
                        ctm.scale(zoom)
                    }
                    val bitmap = synchronized(pageLock) { AndroidDrawDevice.drawPage(page, ctm) }
                    bitmapManager.cacheBitmap(pageKey, bitmap)

                    emit(bitmap)
                } catch (e: Exception) {
                    Log.e(TAG, "getPageBitmap at index $index: ${e.message}")
                    e.printStackTrace()
                    emit(null)
                }
            }
        } else {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun getCurrentPage() {
        val doc = documentId?.let { documentDao.getDocument(it) }
        val book = documentId?.let { bookDao.getBookByMd5(it) }

        currentPage = doc?.currentPage ?: book?.pagesRead ?: 0

        // synchronize currentPage with zero indexing expected by the pager
        if (currentPage > 0) {
            currentPage--
        }
    }

    private suspend fun loadOutline() {
        withContext(Dispatchers.IO) {
            try {
                val outline = document?.loadOutline()

                if (outline == null) {
                    throw Exception()
                } else {
                    flattenOutlineNodes(outline, "")
                    hasOutline = true
                }

                flatOutline.sortBy { it.page }
            } catch (e: Exception) {
                Log.e(TAG, "loadOutline: $docTitle has no outline")
            }
        }
    }

    private fun flattenOutlineNodes(
        list: Array<Outline>,
        indent: String
    ) {
        if (document != null) {
            for (node in list) {
                if (node.title != null) {
                    val page = document!!.pageNumberFromLocation(document!!.resolveLink(node))
                    flatOutline.add(Item(indent + node.title, page, node.uri))
                }
                if (node.down != null) flattenOutlineNodes(node.down, "$indent    ")
            }
        }
    }

    fun getDocumentTitle(): String {
        var title: String

        synchronized(documentAccessLock) {
            title = document?.getMetaData(Document.META_INFO_TITLE)
                ?: docTitle
        }

        return title
    }

    fun authenticate(context: Context) {
        if (document?.authenticatePassword(password) == true) {
            viewModelScope.launch(Dispatchers.IO) {
                documentPageCount = document!!.countPages()
                getCurrentPage()
                loadOutline()
                loadPages()
                getDocumentLinks()

                contentState = ContentState.NOT_LOADING

                hasBookBeenCompleted()
            }

            requiresPassword = false
        } else {
            Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Update stored document info
     */
    suspend fun updateDocumentData() {
        updateJobMutex.withLock {
            val doc = documentId?.let { documentDao.getDocument(it) }
            if (doc != null) {
                val lastRead =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Date.from(Instant.now())
                    } else {
                        Calendar.getInstance().time
                    }

                // adding 1 because the pager and lib are zero indexed
                val modifiedDocument = doc.copy(
                    pages = documentPageCount,
                    currentPage = currentPage + 1,
                    lastRead = lastRead,
                    isRead = (currentPage + 1) == documentPageCount
                )

                documentDao.updateDocument(modifiedDocument)

                if (doc.autoTrackable) {
                    updateBookListEntry(modifiedDocument)
                }
            }
        }
    }

    /**
     * Update an item in the book list with updated progress whenever
     * a document is closed
     *
     * @param document the md5 of the document
     * */
    private suspend fun updateBookListEntry(document: LLDocument) {
        val book = bookDao.getBookByMd5(document.id)
        val isComplete = document.currentPage == document.pages
        val completionDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().toEpochMilli()
        } else {
            Calendar.getInstance().time.time
        }

        book?.let { theBook ->
            if (theBook.pagesRead <= document.currentPage) {
                //                  check if book has not been completed in the past
                if (isComplete && theBook.dateCompleted == null) {
                    readingProgressManager.incrementReadingGoalProgress()
                }

                bookDao.updateBook(
                    theBook.copy(
                        pageCount = document.pages,
                        pagesRead = document.currentPage,
                        coverImageName = if (theBook.coverImageName.isNullOrEmpty()) document.cover else theBook.coverImageName,
                        status = if (document.pages == document.currentPage) BookStatus.COMPLETED.strName else theBook.status,
                        dateCompleted = if (isComplete && theBook.dateCompleted == null) {
                            completionDate
                        } else {
                            theBook.dateCompleted
                        },
                        timesReread = if (isComplete && theBook.status == BookStatus.RE_READING.strName) theBook.timesReread + 1 else theBook.timesReread
                    )
                )
            }
        }
    }

    /**
     * Initiate searching the document for a query text
     *
     * @param noResultAction what to do when no query match is found
     * */
    fun startSearch(noResultAction: () -> Unit) {
        val results = mutableListOf<SearchResult>()
        searchResults = emptyList()
        searchInProgress = true

        viewModelScope.launch(Dispatchers.IO) {
            pages.forEachIndexed { pageNum, page ->
                val hits = page.search(searchQuery)

                hits.forEach { quads ->
                    results.add(
                        SearchResult(
                            pageNumber = pageNum,
                            text = searchQuery,
                            quads = quads
                        )
                    )
                }
            }

            if (results.isNotEmpty()) {
                showSearchResults = true
            } else {
                withContext(Dispatchers.Main) {
                    noResultAction()
                }
            }

            searchResults = results
            searchInProgress = false
        }
    }

    fun moveToNextSearchResult(): Int? {
        try {
            val current = searchResults.first { it.pageNumber > currentPage }

            return current.pageNumber
        } catch (e: Exception) {
            return null
        }
    }

    fun moveToPreviousSearchResult(): Int? {
        try {
            val current = searchResults.last { it.pageNumber < currentPage }

            return current.pageNumber
        } catch (e: Exception) {
            return null
        }
    }

    fun getPreference(key: SettingsRepository.BooleanPreferenceType): Flow<Boolean> {
        return settingsRepository.getPreference(key)
    }

    /**
     * Get the bounds for the page at the specified [index].
     *
     * @return Pair<width, height>
     * */
    fun getPageBounds(index: Int): Pair<Int, Int> {
        val page = pages[index]

        return Pair(page.bounds.x1.toInt(), page.bounds.y1.toInt())
    }

    private fun getDocumentLinks() {
        val result = mutableListOf<LinkItem>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                pages.forEachIndexed { pageNum, page ->
                    result.add(
                        LinkItem(
                            page = pageNum,
                            links = page.links
                        )
                    )
                }

                if (result.isNotEmpty()) {
                    documentLinks = result
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "getDocumentLinks: ${e.message}")
            }
        }
    }

    fun toggleShowLinks() {
        showLinks = !showLinks
    }

    fun getPageIndexFromLink(link: Link): Int? {
        try {
            synchronized(documentAccessLock) {
                return document!!.pageNumberFromLocation(document!!.resolveLink(link))
            }
        } catch (e: Exception) {
            Log.e(TAG, "navigateTo: ${e.message}")
            return null
        }
    }

    fun pushToHistory(value: Int) {
        _visitedPages.value += value
    }

    fun popFromHistory(): Result<Int> {
        if (_visitedPages.value.isNotEmpty()) {
            val last = _visitedPages.value.last()
            _visitedPages.value = _visitedPages.value.dropLast(1)

            return Result.success(last)
        }

        return Result.failure(EmptyHistoryException())
    }

    private fun hasBookBeenCompleted() {
        viewModelScope.launch(Dispatchers.IO) {
            val book = documentId?.let { bookDao.getBookByMd5(it) }

            if (book == null) {
                _showRereadDialog.value = false
            } else {
                _showRereadDialog.value = book.status == BookStatus.COMPLETED.strName
            }
        }
    }

    fun closeRereadDialog() {
        _showRereadDialog.value = false
    }

    suspend fun rereadBook() {
        val book = documentId?.let { bookDao.getBookByMd5(it) }
        book?.let { theBook ->
            bookDao.updateBook(
                theBook.copy(
                    status = BookStatus.RE_READING.strName,
                    pagesRead = 0
                )
            )
            currentPage = 0

            _showRereadDialog.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        document?.destroy()
    }
}

class EmptyHistoryException : Exception()

data class DocumentInfo(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val creationDate: String = "",
    val modDate: String = "",
    val creator: String = "",
    val producer: String = "",
    val keywords: String = "",
    val encryption: String = "",
)
