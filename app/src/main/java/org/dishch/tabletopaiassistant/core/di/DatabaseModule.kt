package org.dishch.tabletopaiassistant.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.dishch.tabletopaiassistant.core.database.AppDatabase
import org.dishch.tabletopaiassistant.core.database.dao.ChatMessageDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "tabletop_ai_assistant.db")
            .build()

    @Provides
    @Singleton
    fun provideChatMessageDao(database: AppDatabase): ChatMessageDao = database.chatMessageDao()
}
