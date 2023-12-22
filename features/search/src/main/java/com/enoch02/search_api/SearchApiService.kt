package com.enoch02.search_api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {

    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("fields") fields: String = "key,title,author_name,isbn,publisher,cover_i,subject,publish_year,language,number_of_pages_median"
    ): SearchResult

    companion object {
        private var searchApiService: SearchApiService? = null

        fun getInstance(): SearchApiService {
            if (searchApiService == null) {
                searchApiService = Retrofit.Builder()
                    .baseUrl("https://openlibrary.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(SearchApiService::class.java)
            }
            return searchApiService!!
        }
    }
}