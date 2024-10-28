package com.artifex.mupdf.viewer.shared

data class Item(
    val title: String,
    val page: Int,
    val uri: String = ""
)