package org.dishch.tabletopaiassistant.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import org.dishch.tabletopaiassistant.core.database.dao.ChatMessageDao
import org.dishch.tabletopaiassistant.core.database.entity.ChatMessageEntity

@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
}
