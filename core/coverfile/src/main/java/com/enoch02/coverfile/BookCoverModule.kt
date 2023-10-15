package com.enoch02.coverfile

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookCoverModule {
    @Provides
    @Singleton
    fun providesBookCoverRepo(@ApplicationContext context: Context) = BookCoverRepository(context)
}