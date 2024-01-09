package com.enoch02.database.export_and_import.csv

import android.app.Application
import android.net.Uri
import android.util.Log
import com.enoch02.database.dao.BookDao
import com.enoch02.database.util.formatEpochDate
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class CSVManager(private val application: Application, private val bookDao: BookDao) {
    private val tempFile = File(application.filesDir, "temp.csv")

    init {
        if (tempFile.exists()) {
            tempFile.writeText("")
        } else {
            tempFile.createNewFile()
        }
    }

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
                        formatEpochDate(it.dateStarted),
                        formatEpochDate(it.dateCompleted),
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
}