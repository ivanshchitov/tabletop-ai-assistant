package org.dishch.tabletopaiassistant.feature.assistant.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.dishch.tabletopaiassistant.feature.assistant.data.datasource.AssistantApi
import org.dishch.tabletopaiassistant.feature.assistant.data.repository.AssistantRepositoryImpl
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class AssistantModule {

    @Binds
    @Singleton
    abstract fun bindAssistantRepository(impl: AssistantRepositoryImpl): AssistantRepository

    companion object {

        @Provides
        @Singleton
        fun provideAssistantApi(retrofit: Retrofit): AssistantApi = retrofit.create(AssistantApi::class.java)
    }
}
