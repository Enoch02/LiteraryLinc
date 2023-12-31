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