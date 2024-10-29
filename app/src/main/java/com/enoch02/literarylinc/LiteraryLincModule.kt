package com.enoch02.literarylinc

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LiteraryLincModule {
    @Provides
    fun providesApplicationContext(@ApplicationContext context: Context) = context
}
