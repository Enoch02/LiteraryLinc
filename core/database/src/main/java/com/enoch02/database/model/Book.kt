package com.enoch02.database.model


data class Book(
    val id: Int? = null,
    val type: BookType,
    val title: String,
    val authors: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val coverImage: String? = null
)
