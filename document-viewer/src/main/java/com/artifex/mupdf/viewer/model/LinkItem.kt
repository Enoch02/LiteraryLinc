package com.artifex.mupdf.viewer.model

import com.artifex.mupdf.fitz.Link

data class LinkItem(
    val page: Int,
    val links: Array<Link>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinkItem

        return page == other.page
    }

    override fun hashCode(): Int {
        return page
    }
}