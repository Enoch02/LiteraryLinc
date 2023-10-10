package com.enoch02.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val type: BookType,
    /*val authors: List<String> = emptyList(),
    val genres: List<String> = emptyList(),*/
    val coverImage: String? = null  //TODO: use something more appropriate
)
