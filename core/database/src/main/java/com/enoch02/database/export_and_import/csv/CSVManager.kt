package com.enoch02.database.export_and_import.csv

import android.app.Application
import android.net.Uri
import android.util.Log
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.database.util.formatEpochAsString
import com.enoch02.database.util.getEpochFromString
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CSVManager(private val application: Application, private val bookDao: BookDao) {

    //TODO: add error handling and return Result object
    suspend fun export(uri: Uri) = withContext(Dispatchers.IO) {
        val contentResolver = application.contentResolver
        val outputStream = contentResolver.openOutputStream(uri)
        val books = bookDao.getBooksNonFlow()

        if (outputStream != null) {
            csvWriter().open(outputStream) {
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
                        "Cover Image Name",
                        "Notes",
                        "Status"
                    )
                )

                books.forEach {
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
                        it.coverImageName,
                        it.notes,
                        it.status
                    )

                    writeRow(row)
                }
            }
        }
    }

    //TODO: handle potential errors
    suspend fun import(uri: Uri) = withContext(Dispatchers.IO) {
        val contentResolver = application.contentResolver
        val csvFileStream = contentResolver.openInputStream(uri)

        if (csvFileStream != null) {
            csvReader().openAsync(csvFileStream) {
                readAllAsSequence().forEachIndexed { index, row ->
                    if (index != 0) {
                        try {
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
                                coverImageName = row[11],
                                notes = row[12],
                                status = row[13]
                            )

                            bookDao.insertBook(book)
                        } catch (e: Exception) {
                            Log.e("TAG", "import: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}