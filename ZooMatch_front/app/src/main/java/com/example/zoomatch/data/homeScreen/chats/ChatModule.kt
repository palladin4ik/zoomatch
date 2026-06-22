package com.example.zoomatch.data.homeScreen.chats

import android.content.Context
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.Network
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.WebSocketManager
import com.example.zoomatch.data.db.ZooMatchApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

  @Provides
  @Singleton
  fun provideAppDatabase(
    @ApplicationContext context: Context
  ): AppDatabase {
    return AppDatabase.getDatabase(context)
  }

  @Provides
  @Singleton
  fun provideUserDao(appDatabase: AppDatabase): UserDao {
    return appDatabase.userDao()
  }

  @Provides
  @Singleton
  fun provideTokenManager(
    @ApplicationContext context: Context
  ): TokenManager {
    return TokenManager(context)
  }

  @Provides
  @Singleton
  fun provideZooMatchApi(): ZooMatchApi {
    return Network.zooMatchApi
  }

  @Provides
  @Singleton
  fun provideCurrentUserProvider(
    tokenManager: TokenManager,
    appDatabase: AppDatabase
  ): CurrentUserProvider {
    return CurrentUserProvider(tokenManager, appDatabase.userDao())
  }

  @Provides
  @Singleton
  fun provideWebSocketManager(messagesRepository: MessagesRepository): WebSocketManager {
    return WebSocketManager(messagesRepository)
  }

  @Provides
  @Singleton
  fun provideChatsRemoteDataSource(
    api: ZooMatchApi,
    currentUserProvider: CurrentUserProvider
  ): ChatsRemoteDataSource {
    return ChatsRemoteDataSource(api, currentUserProvider)
  }

  @Provides
  @Singleton
  fun provideChatsRepository(
    appDatabase: AppDatabase,
    remoteDataSource: ChatsRemoteDataSource
  ): ChatsRepository {
    return ChatsRepository(appDatabase, remoteDataSource)
  }

  @Provides
  @Singleton
  fun provideMessagesRepository(appDatabase: AppDatabase): MessagesRepository {
    return MessagesRepository(appDatabase)
  }

  @Provides
  @Singleton
  fun provideFileAttachmentHelper(
    @ApplicationContext context: Context
  ): FileAttachmentHelper = FileAttachmentHelper(context)
}
