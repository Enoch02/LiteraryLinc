package com.enoch02.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val type: BookType,
    val title: String,
    /*val authors: List<String> = emptyList(),
    val genres: List<String> = emptyList(),*/
    val coverImage: String? = null  //TODO: use something more appropriate
)
