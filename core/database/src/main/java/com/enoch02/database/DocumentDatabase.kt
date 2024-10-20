package com.enoch02.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enoch02.database.converters.DateTypeConverter
import com.enoch02.database.converters.UriTypeConverter
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.LLDocument

@Database(
    entities = [LLDocument::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ],
    exportSchema = true
)
@TypeConverters(UriTypeConverter::class, DateTypeConverter::class)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun getDocumentDao(): DocumentDao
}