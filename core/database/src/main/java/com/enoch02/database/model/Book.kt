package com.enoch02.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val type: BookType,
    /*TODO: create type converters for the lists*/
    /*val authors: List<String> = emptyList(),
    val genres: List<String> = emptyList(),*/
    val coverImageName: String? = null  //TODO: Save path insted?
) {
    companion object {
        fun createBook(title: String, type: BookType, coverImageName: String? = null): Book {
            if (title.isEmpty())
                throw Exception("Please enter a Title")

            return Book(title = title, type = type, coverImageName = coverImageName)
        }
    }
}

