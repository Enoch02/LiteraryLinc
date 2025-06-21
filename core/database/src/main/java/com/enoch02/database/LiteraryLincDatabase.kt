package com.enoch02.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book

@Database(
    entities = [Book::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class LiteraryLincDatabase : RoomDatabase() {
    abstract fun getBookDao(): BookDao
}