package com.enoch02.search_api

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchApiModule {
    @Provides
    @Singleton
    fun providesSearchApiService(): SearchApiService = SearchApiService.getInstance()
}