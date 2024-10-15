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

    @Query(
        value = """
    SELECT * FROM documents 
    ORDER BY 
        CASE 
            WHEN name GLOB '*[0-9]*' THEN CAST(name AS INTEGER) 
            ELSE 0 
        END, 
        name ASC
    """
    )
    fun getDocumentsByName(): Flow<List<LLDocument>>

    @Query(value = "SELECT * FROM documents ORDER BY lastRead DESC")
    fun getDocumentsByLastRead(): Flow<List<LLDocument>>

    @Query(value = "SELECT * FROM documents ORDER BY sizeInMb DESC")
    fun getDocumentsBySize(): Flow<List<LLDocument>>

    @Query(value = "SELECT * FROM documents ORDER BY type ASC")
    fun getDocumentsByFormat(): Flow<List<LLDocument>>

    @Query(value = "SELECT * FROM documents")
    suspend fun getDocumentsNonFlow(): List<LLDocument>

    @Update
    suspend fun updateDocument(document: LLDocument)

    @Query(value = "DELETE FROM documents WHERE contentUri = :uriString")
    suspend fun deleteDocument(uriString: String)
}