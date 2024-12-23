package com.enoch02.database.model

enum class ReaderFilter(val value: String) {
    ALL("Books and documents"),
    READING("Reading"),
    FAVORITES("Favorites"),
    COMPLETED("Completed"),
    NO_FILE("Has no file")
}