package com.enoch02.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enoch02.database.dao.SearchHistoryDao
import com.enoch02.database.model.HistoryItem

@Database(entities = [HistoryItem::class], version = 1, exportSchema = true)
abstract class SearchHistoryDatabase : RoomDatabase() {

    abstract fun getSearchHistoryDao(): SearchHistoryDao
}