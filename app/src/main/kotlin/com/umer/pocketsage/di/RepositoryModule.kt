package com.umer.pocketsage.di

import com.umer.pocketsage.data.repository.DocumentRepositoryImpl
import com.umer.pocketsage.data.repository.RetrieverImpl
import com.umer.pocketsage.domain.DocumentRepository
import com.umer.pocketsage.domain.Retriever
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindRetriever(impl: RetrieverImpl): Retriever
}
