package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.enoch02.database.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert()
    suspend fun insertDocuments(book: List<Document>)

    @Query(value = "SELECT * FROM documents")
    fun getDocuments(): Flow<List<Document>>

    @Query(value = "SELECT * FROM documents")
    suspend fun getDocumentsNonFlow(): List<Document>

    @Update
    suspend fun updateDocument(document: Document)
}