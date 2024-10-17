package com.enoch02.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enoch02.database.model.LLDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDocuments(book: List<LLDocument>)

    @Query(value = "SELECT * FROM documents")
    fun getDocuments(): Flow<List<LLDocument>>

    @Query(value = "SELECT * FROM documents")
    suspend fun getDocumentsNonFlow(): List<LLDocument>

    @Update
    suspend fun updateDocument(document: LLDocument)

    @Query(value = "DELETE FROM documents WHERE contentUri = :uriString")
    suspend fun deleteDocument(uriString: String)
}