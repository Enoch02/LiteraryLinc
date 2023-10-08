package com.enoch02.database

import com.enoch02.database.dao.BookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DaosModule {
    @Provides
    fun providesBookDao(database: LiteraryLincDatabase): BookDao = database.bookDao()
}