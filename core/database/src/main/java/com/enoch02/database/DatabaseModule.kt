package com.enoch02.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val BOOK_LIST_DB_NAME = "literary-linc-db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesLiteraryLincDatabase(@ApplicationContext context: Context): LiteraryLincDatabase =
        Room.databaseBuilder(
            context,
            LiteraryLincDatabase::class.java,
            BOOK_LIST_DB_NAME
        ).build()

    @Provides
    @Singleton
    fun providesSearchHistoryDatabase(@ApplicationContext context: Context): SearchHistoryDatabase =
        Room.databaseBuilder(
            context,
            SearchHistoryDatabase::class.java,
            "search-history-db"
        ).build()

    @Provides
    @Singleton
    fun providesDocumentsDatabase(@ApplicationContext context: Context): DocumentDatabase =
        Room.databaseBuilder(
            context,
            DocumentDatabase::class.java,
            "documents-db"
        ).build()
}