package com.enoch02.database

import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DaosModule {
    @Provides
    fun providesBookDao(database: LiteraryLincDatabase): BookDao = database.getBookDao()

    @Provides
    fun providesSearchHistoryDao(database: SearchHistoryDatabase): SearchHistoryDao =
        database.getSearchHistoryDao()
}