package org.dishch.tabletopaiassistant.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.dishch.tabletopaiassistant.core.prompt.SystemPromptProvider
import org.dishch.tabletopaiassistant.core.prompt.SystemPromptProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PromptModule {

    @Binds
    @Singleton
    abstract fun bindSystemPromptProvider(impl: SystemPromptProviderImpl): SystemPromptProvider
}
