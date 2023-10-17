package com.enoch02.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val type: String,
    /*TODO: create type converters for the lists*/
    /*val authors: List<String> = emptyList(),
    val genres: List<String> = emptyList(),*/
    val coverImageName: String? = null
) {
    companion object {
        val types = mapOf(0 to "Any", 1 to "Comic", 2 to "Light Novel", 3 to "Manga", 4 to "Novel")

        fun createBook(title: String, type: String, coverImageName: String? = null): Book {
            if (title.isEmpty())
                throw Exception("Please enter a Title")

            return Book(title = title, type = type, coverImageName = coverImageName)
        }
    }
}

