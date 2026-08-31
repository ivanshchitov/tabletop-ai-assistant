package org.dishch.tabletopaiassistant.feature.settings.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.dishch.tabletopaiassistant.feature.settings.data.repository.SettingsRepositoryImpl
import org.dishch.tabletopaiassistant.feature.settings.domain.repository.SettingsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
