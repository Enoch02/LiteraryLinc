package com.enoch02.database.model

enum class StatusFilter {
    ALL,
    READING,
    COMPLETED,
    ON_HOLD,
    PLANNING;

    fun stringify(): String {
        return this.name.lowercase().replaceFirstChar { c -> c.uppercase() }.replace("_", " ")
    }
}