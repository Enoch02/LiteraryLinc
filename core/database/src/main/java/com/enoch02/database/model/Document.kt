package com.enoch02.database.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val contentUri: Uri?,
    val name: String,
    val cover: String = ""
)