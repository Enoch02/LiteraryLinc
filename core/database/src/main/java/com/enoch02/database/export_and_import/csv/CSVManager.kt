package com.enoch02.database.export_and_import.csv

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.database.util.Base64Functions
import com.enoch02.database.util.formatEpochAsString
import com.enoch02.database.util.getEpochFromString
import com.opencsv.CSVWriter
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.enums.CSVReaderNullFieldIndicator
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private const val TAG = "CSVManager"

class CSVManager(
    private val application: Application,
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository
) {
    suspend fun export(uri: Uri) {
        val contentResolver = application.contentResolver
        val outputStream = contentResolver.openOutputStream(uri)
        val books = bookDao.getBooksNonFlow()
        val covers = bookCoverRepository.latestCoverPath

        if (outputStream != null) {
            val writer = OutputStreamWriter(outputStream)
            val csvWriter = CSVWriter(writer)

            val header = arrayOf(
                "Title",
                "Author",
                "Pages Read",
                "Page Count",
                "Date Started",
                "Date Completed",
                "Times Reread",
                "Personal Rating",
                "ISBN",
                "Genre",
                "Type",
                "Cover Image[Base64]",
                "Notes",
                "Status",
                "Linked file md5"
            )

            csvWriter.writeNext(header)
            books.forEach {
                val coverImagePath = covers.first()[it.coverImageName]
                val encodedImageResult = Base64Functions.encode(coverImagePath.toString())
                var encodedImage = ""

                encodedImageResult.onSuccess { encodedStr ->
                    encodedImage = encodedStr
                }

                val row = arrayOf(
                    it.title,
                    it.author,
                    it.pagesRead.toString(),
                    it.pageCount.toString(),
                    formatEpochAsString(it.dateStarted),
                    formatEpochAsString(it.dateCompleted),
                    it.timesReread.toString(),
                    it.personalRating.toString(),
                    it.isbn,
                    it.genre,
                    it.type,
                    encodedImage,
                    it.notes,
                    it.status,
                    it.documentMd5
                )

                csvWriter.writeNext(row)
                Log.d(TAG, "export: ${it.title} exported")
            }

            csvWriter.flush()
            csvWriter.close()
        }
    }

    suspend fun excelFriendlyExport(uri: Uri) {
        val contentResolver = application.contentResolver
        val outputStream = contentResolver.openOutputStream(uri)
        val books = bookDao.getBooksNonFlow().sortedBy { it.title }

        if (outputStream != null) {
            val writer = OutputStreamWriter(outputStream)
            val csvWriter = CSVWriter(writer)

            val header = arrayOf(
                "Title",
                "Author",
                "Pages Read",
                "Page Count",
                "Date Started",
                "Date Completed",
                "Times Reread",
                "Personal Rating",
                "ISBN",
                "Genre",
                "Type",
                "Notes",
                "Status"
            )

            csvWriter.writeNext(header)
            books.forEach {
                val row = arrayOf(
                    it.title,
                    it.author,
                    it.pagesRead.toString(),
                    it.pageCount.toString(),
                    formatEpochAsString(it.dateStarted),
                    formatEpochAsString(it.dateCompleted),
                    it.timesReread.toString(),
                    it.personalRating.toString(),
                    it.isbn,
                    it.genre,
                    it.type,
                    it.notes,
                    it.status
                )

                csvWriter.writeNext(row)
                Log.d(TAG, "export: ${it.title} exported")
            }

            csvWriter.flush()
            csvWriter.close()
        }
    }

    fun import(uri: Uri): Result<Unit> {
        try {
            val inputStream = application.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val csvBean = CsvToBeanBuilder<CSVRestoreObject>(reader)
                    .withType(CSVRestoreObject::class.java)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)
                    .build()
                val iterator = csvBean.iterator()

                runBlocking {
                    val jobs = mutableListOf<Deferred<Unit>>()

                    while (iterator.hasNext()) {
                        val row = iterator.next()

                        try {
                            // Launch a coroutine to process the row
                            val job = async(Dispatchers.IO) {
                                processRow(row)
                            }

                            jobs.add(job)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error while processing row: ${e.message}")
                        }
                    }

                    // Wait for all jobs to complete
                    jobs.awaitAll()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "import: ${e.message}")
            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    private suspend fun processRow(restoreObj: CSVRestoreObject) {
        Log.d(TAG, "import: Restoring ${restoreObj.title}")

        if (!bookDao.doesBookTitleExist(restoreObj.title).first()) {
            val decodedImageResult = restoreObj.coverImageMd5?.let {
                Base64Functions.decode(
                    it
                )
            }
            var decodedImage: Bitmap? = null

            decodedImageResult?.onSuccess { bitmap ->
                decodedImage = bitmap
            }

            bookDao.insertBook(
                Book(
                    title = restoreObj.title,
                    author = restoreObj.author,
                    pagesRead = restoreObj.pagesRead,
                    pageCount = restoreObj.pageCount,
                    dateStarted = getEpochFromString(restoreObj.dateStarted ?: ""),
                    dateCompleted = getEpochFromString(restoreObj.dateCompleted ?: ""),
                    timesReread = restoreObj.timesReread,
                    personalRating = restoreObj.personalRating,
                    isbn = restoreObj.isbn,
                    genre = restoreObj.genre,
                    type = restoreObj.type,
                    coverImageName = bookCoverRepository.copyCoverFrom(
                        bitmap = decodedImage,
                        name = restoreObj.title
                    ),
                    notes = restoreObj.notes,
                    status = restoreObj.status,
                    documentMd5 = restoreObj.documentMd5
                )
            )
        } else {
            Log.i(TAG, "processRow: skipping ${restoreObj.title}, title already in db")
        }
    }
}


data class CSVRestoreObject(
    @CsvBindByName(column = "Title", required = true)
    val title: String = "",

    @CsvBindByName(column = "Author")
    val author: String = "",

    @CsvBindByName(column = "Pages Read")
    val pagesRead: Int = 0,

    @CsvBindByName(column = "Page Count")
    val pageCount: Int = 0,

    @CsvBindByName(column = "Date Started")
    val dateStarted: String? = null,

    @CsvBindByName(column = "Date Completed")
    val dateCompleted: String? = null,

    @CsvBindByName(column = "Times Reread")
    val timesReread: Int = 0,

    @CsvBindByName(column = "Personal Rating")
    val personalRating: Int = 0,

    @CsvBindByName(column = "ISBN")
    val isbn: String = "",

    @CsvBindByName(column = "Genre")
    val genre: String = "",

    @CsvBindByName(column = "Type")
    val type: String = "",

    @CsvBindByName(column = "Cover Image[Base64]")
    val coverImageMd5: String? = null,

    @CsvBindByName(column = "Notes")
    val notes: String = "",

    @CsvBindByName(column = "Status")
    val status: String = "",

    @CsvBindByName(column = "Linked file md5")
    val documentMd5: String? = null
)