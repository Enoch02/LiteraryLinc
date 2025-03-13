package com.enoch02.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String = "",
    val author: String = "",
    val pagesRead: Int = 0,
    val pageCount: Int = 0,
    val dateStarted: Long? = null,
    val dateCompleted: Long? = null,
    val timesReread: Int = 0,
    val personalRating: Int = 0,
    val isbn: String = "",
    val genre: String = "",
    val type: String = BookType.ANY.strName,
    val coverImageName: String? = null,
    val notes: String = "",
    val status: String = BookStatus.PLANNING.strName,
    val volumesRead: Int = 0,
    val totalVolumes: Int = 0,
    val documentMd5: String? = null
) {
    companion object {
        enum class BookType(val strName: String) {
            ANY("Any"),
            NON_FICTION("Non-Fiction"),
            LN("Light Novel"),
            NOVEL("Novel"),
            COMIC("Comic"),
            MANGA("Manga")
        }

        enum class BookStatus(val strName: String) {
            READING("Reading"),
            COMPLETED("Completed"),
            ON_HOLD("On Hold"),
            PLANNING("Planning"),
            RE_READING("Rereading")
        }
        //val status = listOf("Reading", "Completed", "On Hold", "Planning", "Rereading")

        fun createBook(
            id: Int? = null,
            title: String,
            author: String,
            pagesRead: String,
            pageCount: String,
            dateStarted: Long?,
            dateCompleted: Long?,
            timesReread: String,
            personalRating: String,
            isbn: String,
            genre: String,
            type: String,
            coverImageName: String?,
            notes: String,
            status: String,
            volumesRead: String,
            totalVolumes: String,
            documentMd5: String? = null
        ): Book {
            when {
                title.isEmpty() -> {
                    throw Exception("Please enter a Title")
                }

                pagesRead.isEmpty() -> {
                    throw Exception("Please enter the Pages Read")
                }

                pageCount.isEmpty() -> {
                    throw Exception("Please enter the Page Count")
                }

                volumesRead.isEmpty() -> {
                    throw Exception("Please enter the Volumes Read")
                }

                totalVolumes.isEmpty() -> {
                    throw Exception("Please enter the Total Volumes")
                }

                (dateStarted != null && dateCompleted != null) -> {
                    if (dateCompleted < dateStarted) {
                        throw Exception("Completion date can not come before start date")
                    }
                }
            }

            return Book(
                id = id,
                title = title,
                author = author,
                pagesRead = pagesRead.toInt(),
                pageCount = pageCount.toInt(),
                dateStarted = dateStarted,
                dateCompleted = dateCompleted,
                timesReread = timesReread.toInt(),
                personalRating = personalRating.toInt(),
                isbn = isbn,
                genre = genre,
                type = type,
                coverImageName = coverImageName,
                notes = notes,
                status = status,
                volumesRead = volumesRead.toInt(),
                totalVolumes = totalVolumes.toInt(),
                documentMd5 = documentMd5
            )
        }
    }
}
