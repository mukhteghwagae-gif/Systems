package systems.nzr1.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import systems.nzr1.data.local.entity.BookmarkEntity
import systems.nzr1.data.local.entity.ChatMessageEntity
import systems.nzr1.data.local.entity.TerminalCommandEntity

@Dao
interface TerminalDao {
    @Query("SELECT * FROM terminal_history ORDER BY timestamp DESC LIMIT 500")
    fun getHistory(): Flow<List<TerminalCommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cmd: TerminalCommandEntity)

    @Query("DELETE FROM terminal_history")
    suspend fun clearAll()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: ChatMessageEntity)

    @Query("DELETE FROM chat_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM chat_history")
    suspend fun count(): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(b: BookmarkEntity)

    @Delete
    suspend fun delete(b: BookmarkEntity)
}
