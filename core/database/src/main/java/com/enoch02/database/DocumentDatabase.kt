package com.enoch02.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.enoch02.database.converters.UriTypeConverter
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Document

@Database(entities = [Document::class], version = 1, exportSchema = false)
@TypeConverters(UriTypeConverter::class)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun getDocumentDao(): DocumentDao
}