package com.enoch02.database.model

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.IllegalArgumentException
import java.util.UUID

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey
    val id: String,
    val contentUri: Uri?,
    val name: String,
    val cover: String = ""
)

fun Document.existsAsFile(context: Context): Boolean {
    try {
        val documentFile =
            contentUri?.let { DocumentFile.fromSingleUri(context, it) } ?: return false

        return documentFile.exists() && documentFile.isFile
    } catch (e: Exception) {
        return false
    }
}