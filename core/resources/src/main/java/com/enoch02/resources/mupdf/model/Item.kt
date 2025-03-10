package com.enoch02.resources.mupdf.model

data class Item(
    val title: String,
    val page: Int,
    val uri: String = ""
)