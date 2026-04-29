package com.umer.pocketsage.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DocumentEntity::class, ChunkEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RagDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun chunkDao(): ChunkDao
}