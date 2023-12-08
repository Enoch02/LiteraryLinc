package com.enoch02.search_api

import com.google.gson.annotations.SerializedName

// Default JSON response from API. Does not handle all response keys
data class SearchResult(
    val numFound: Int,
    val docs: List<Doc>
)

//TODO: One of the values might return null for some of its instances. Find and fix it
data class Doc(
    val key: String,
    val title: String,
    @SerializedName("publish_year") val publishYear: List<String>,
    val isbn: List<String>,
    @SerializedName("cover_i") val coverId: String,
    val publisher: List<String>,
    val language: List<String>,
    @SerializedName("author_name") val author: List<String>,
    val subject: List<String>
)