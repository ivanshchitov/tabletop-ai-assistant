package org.dishch.tabletopaiassistant.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.dishch.tabletopaiassistant.core.resources.ResourceProvider
import org.dishch.tabletopaiassistant.core.resources.ResourceProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourceModule {

    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: ResourceProviderImpl): ResourceProvider
}
