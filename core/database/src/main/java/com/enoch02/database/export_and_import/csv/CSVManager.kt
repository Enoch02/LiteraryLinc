package com.enoch02.database.export_and_import.csv

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.database.util.Base64Functions
import com.enoch02.database.util.formatEpochAsString
import com.enoch02.database.util.getEpochFromString
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


//TODO: Figure out how to run this in the background even when the app is closed.
class CSVManager(
    private val application: Application,
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository
) {

    //TODO: add error handling and return Result object
    suspend fun export(uri: Uri) {
        val contentResolver = application.contentResolver
        val outputStream = contentResolver.openOutputStream(uri)
        val books = bookDao.getBooksNonFlow()
        val covers = bookCoverRepository.latestCoverPath

        if (outputStream != null) {
            csvWriter().openAsync(outputStream) {
                writeRow(
                    listOf(
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
                )

                books.forEach {
                    val coverImagePath = covers.first()[it.coverImageName]
                    val encodedImageResult = Base64Functions.encode(coverImagePath.toString())
                    var encodedImage = ""

                    encodedImageResult.onSuccess { encodedStr ->
                        encodedImage = encodedStr
                    }

                    val row = listOf(
                        it.title,
                        it.author,
                        it.pagesRead,
                        it.pageCount,
                        formatEpochAsString(it.dateStarted),
                        formatEpochAsString(it.dateCompleted),
                        it.timesReread,
                        it.personalRating,
                        it.isbn,
                        it.genre,
                        it.type,
                        encodedImage,
                        it.notes,
                        it.status,
                        it.documentMd5
                    )

                    writeRow(row)
                }
            }
        }
    }

    suspend fun import(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val contentResolver = application.contentResolver
        val csvFileStream = contentResolver.openInputStream(uri)

        if (csvFileStream != null) {
            try {
                csvReader().openAsync(csvFileStream) {
                    readAllAsSequence().forEachIndexed { index, row ->
                        if (index != 0) {
                            val encodedImage = row[11]
                            val decodedImageResult = Base64Functions.decode(encodedImage)
                            var decodedImage: Bitmap? = null

                            decodedImageResult.onSuccess { bitmap ->
                                decodedImage = bitmap
                            }

                            val book = Book(
                                title = row[0],
                                author = row[1],
                                pagesRead = row[2].toInt(),
                                pageCount = row[3].toInt(),
                                dateStarted = getEpochFromString(row[4]),
                                dateCompleted = getEpochFromString(row[5]),
                                timesReread = row[6].toInt(),
                                personalRating = row[7].toInt(),
                                isbn = row[8],
                                genre = row[9],
                                type = row[10],
                                coverImageName = bookCoverRepository.copyCoverFrom(
                                    bitmap = decodedImage,
                                    name = row[0]
                                ),
                                notes = row[12],
                                status = row[13],
                                documentMd5 = row[14]
                            )

                            bookDao.insertBook(book)
                        }
                    }
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

        return@withContext Result.success(Unit)
    }
}