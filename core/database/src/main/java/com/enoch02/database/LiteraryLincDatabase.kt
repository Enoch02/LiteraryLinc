package com.enoch02.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book

@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class LiteraryLincDatabase : RoomDatabase() {
    abstract fun getBookDao(): BookDao
}