package com.enoch02.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesLiteraryLincDatabase(@ApplicationContext context: Context): LiteraryLincDatabase =
        Room.databaseBuilder(
            context,
            LiteraryLincDatabase::class.java,
            "literary-linc-db"
        ).build()
}