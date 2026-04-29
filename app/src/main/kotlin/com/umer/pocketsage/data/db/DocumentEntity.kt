package com.umer.pocketsage.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val uri: String,
    val createdAt: Long,
    val chunkCount: Int,
)