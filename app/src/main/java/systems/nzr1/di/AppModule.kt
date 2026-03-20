package systems.nzr1.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import systems.nzr1.data.local.AppDatabase
import systems.nzr1.data.local.dao.BookmarkDao
import systems.nzr1.data.local.dao.ChatDao
import systems.nzr1.data.local.dao.TerminalDao
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "systems_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideTerminalDao(db: AppDatabase): TerminalDao   = db.terminalDao()
    @Provides fun provideChatDao(db: AppDatabase): ChatDao           = db.chatDao()
    @Provides fun provideBookmarkDao(db: AppDatabase): BookmarkDao   = db.bookmarkDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
}
