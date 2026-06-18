package com.example.zoomatch.data.homeScreen.chats

import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.ChatEntity
import com.example.zoomatch.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatsRepository @Inject constructor(
  private val db: AppDatabase,
  private val remoteDataSource: ChatsRemoteDataSource
) : ChatsDataSource {

  override fun getAllChats(currentUserId: Int): Flow<List<ChatItem>> {
    return db.chatDao().getAllChatsFlow().map { entities ->
      entities.map { entity ->
        ChatItem(
          chatId = entity.chatId,
          interlocutorId = entity.interlocutorId,
          name = entity.name,
          avatar = entity.avatar,
          lastMessage = entity.lastMessage,
          lastMessageTime = entity.lastMessageTime,
          unreadCount = entity.unreadCount
        )
      }
    }
  }

  suspend fun fetchChatsFromServer(currentUserId: Int) {
    try {
      remoteDataSource.getAllChats(currentUserId).collect { remoteChats ->
        val entities = remoteChats.map { chatItem ->
          ChatEntity(
            chatId = chatItem.chatId,
            interlocutorId = chatItem.interlocutorId,
            name = chatItem.name,
            avatar = chatItem.avatar,
            lastMessage = chatItem.lastMessage,
            lastMessageTime = chatItem.lastMessageTime ?: System.currentTimeMillis(),
            unreadCount = chatItem.unreadCount
          )
        }
        db.chatDao().insertAll(entities)
      }
    } catch (e: Exception) {
      // Игнорируем ошибки сети: UI продолжит отображать кэш из Room
    }
  }

  override suspend fun getLastMessageForChat(chatId: String): MessageEntity? {
    return db.messageDao().getLastMessages(chatId, 1).firstOrNull()
  }
}