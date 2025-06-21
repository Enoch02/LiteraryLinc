package com.enoch02.database

import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DaosModule {
    @Provides
    fun providesBookDao(database: LiteraryLincDatabase): BookDao = database.getBookDao()

    @Provides
    @Singleton
    fun providesDocumentsDao(database: DocumentDatabase): DocumentDao = database.getDocumentDao()
}
