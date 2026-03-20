package systems.nzr1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import systems.nzr1.data.local.dao.BookmarkDao
import systems.nzr1.data.local.dao.ChatDao
import systems.nzr1.data.local.dao.TerminalDao
import systems.nzr1.data.local.entity.BookmarkEntity
import systems.nzr1.data.local.entity.ChatMessageEntity
import systems.nzr1.data.local.entity.TerminalCommandEntity

@Database(
    entities = [
        TerminalCommandEntity::class,
        ChatMessageEntity::class,
        BookmarkEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun terminalDao(): TerminalDao
    abstract fun chatDao(): ChatDao
    abstract fun bookmarkDao(): BookmarkDao
}
