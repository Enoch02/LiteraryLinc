package com.enoch02.resources.mupdf.model

import com.artifex.mupdf.fitz.Quad

data class SearchResult(
    val pageNumber: Int,
    val text: String,
    val quads: Array<Quad>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResult

        return pageNumber == other.pageNumber
    }

    override fun hashCode(): Int {
        return pageNumber
    }
}
