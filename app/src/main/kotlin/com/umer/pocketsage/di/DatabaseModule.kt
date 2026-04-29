package com.umer.pocketsage.di

import android.content.Context
import androidx.room.Room
import com.umer.pocketsage.data.db.ChunkDao
import com.umer.pocketsage.data.db.DocumentDao
import com.umer.pocketsage.data.db.RagDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRagDatabase(@ApplicationContext context: Context): RagDatabase =
        Room.databaseBuilder(context, RagDatabase::class.java, "rag_database")
            .build()

    @Provides
    fun provideDocumentDao(db: RagDatabase): DocumentDao = db.documentDao()

    @Provides
    fun provideChunkDao(db: RagDatabase): ChunkDao = db.chunkDao()
}