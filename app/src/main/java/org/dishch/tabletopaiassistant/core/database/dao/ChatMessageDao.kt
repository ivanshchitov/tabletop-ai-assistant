package org.dishch.tabletopaiassistant.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.core.database.entity.ChatMessageEntity

@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query(
        """
        SELECT * FROM (
            SELECT * FROM chat_message ORDER BY id DESC LIMIT :limit
        ) ORDER BY id ASC
        """,
    )
    fun observeLastMessages(limit: Int = MAX_HISTORY_SIZE): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_message WHERE role = 'user'")
    fun observeDialogCount(): Flow<Int>

    @Query("DELETE FROM chat_message")
    suspend fun deleteAllMessages()

    companion object {
        const val MAX_HISTORY_SIZE = 50
    }
}
