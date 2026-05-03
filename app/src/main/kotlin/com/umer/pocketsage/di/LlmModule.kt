package com.umer.pocketsage.di

import com.umer.pocketsage.data.llm.LiteRtLmRunner
import com.umer.pocketsage.domain.LlmRunner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LlmModule {

    @Binds
    @Singleton
    abstract fun bindLlmRunner(impl: LiteRtLmRunner): LlmRunner
}