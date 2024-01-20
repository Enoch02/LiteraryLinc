package com.enoch02.database.export_and_import

import android.app.Application
import com.enoch02.database.dao.BookDao
import com.enoch02.database.export_and_import.csv.CSVManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ExportImportModule {
    @Provides
    fun providesCSVManager(application: Application, bookDao: BookDao) = CSVManager(application, bookDao)
}