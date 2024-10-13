package com.enoch02.database.model

enum class Sorting {
    ALPHABETICAL,
    ALPHABETICAL_REVERSE,
    DATE_STARTED,
    DATE_STARTED_REVERSE;

    fun stringify(): String {
        return this.name.lowercase().replaceFirstChar { c -> c.uppercase() }.replace("_", " ")
    }
}

enum class ReaderSorting(val value: String) {
    NAME("Name"),
    LAST_READ("Last read"),
    SIZE("File size"),
    FORMAT("File format")
}