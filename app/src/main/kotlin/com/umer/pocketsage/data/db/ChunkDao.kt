package com.umer.pocketsage.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChunkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chunks: List<ChunkEntity>)

    @Query("SELECT * FROM chunks WHERE documentId IN (:ids)")
    suspend fun getForDocuments(ids: List<String>): List<ChunkEntity>

    @Query("SELECT * FROM chunks")
    suspend fun getAll(): List<ChunkEntity>

    @Query("DELETE FROM chunks WHERE documentId = :id")
    suspend fun deleteForDocument(id: String)
}