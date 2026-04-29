package com.umer.pocketsage.di

import com.umer.pocketsage.data.embedding.TfLiteEmbeddingService
import com.umer.pocketsage.domain.EmbeddingService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EmbeddingModule {

    @Binds
    @Singleton
    abstract fun bindEmbeddingService(impl: TfLiteEmbeddingService): EmbeddingService

    companion object {
        @Provides
        @Named("modelAssetPath")
        fun provideModelAssetPath(): String = "embedding/all-MiniLM-L6-v2.tflite"

        @Provides
        @Named("vocabAssetPath")
        fun provideVocabAssetPath(): String = "embedding/vocab.txt"
    }
}
