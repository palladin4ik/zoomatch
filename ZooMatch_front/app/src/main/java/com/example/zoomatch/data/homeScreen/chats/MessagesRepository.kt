package com.example.zoomatch.data.homeScreen.chats

import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Репозиторий для управления сообщениями чата.
 * Реализует интерфейс MessagesDataSource и координирует работу с локальной БД Room.
 */
class MessagesRepository @Inject constructor(
  private val db: AppDatabase
) : MessagesDataSource {

  override fun getMessages(chatId: String): Flow<List<MessageEntity>> {
    return db.messageDao().getMessagesForChat(chatId)
  }

  override suspend fun insertMessage(message: MessageEntity): Long {
    return db.messageDao().insert(message)
  }

  // Вставка списка сообщений (используется при синхронизации истории с сервером)
  suspend fun insertAll(messages: List<MessageEntity>) {
    db.messageDao().insertAll(messages)
  }

  override suspend fun markAsRead(messageId: Long, currentUserId: Int) {
    db.messageDao().markAsRead(messageId, currentUserId)
  }

  override suspend fun markAsDelivered(messageId: Long) {
    db.messageDao().markAsDelivered(messageId)
  }

  override suspend fun deleteAllMessagesInChat(chatId: String) {
    db.messageDao().deleteChatMessages(chatId)
  }

  suspend fun updateMessageText(serverId: Int, newText: String) {
    db.messageDao().updateText(serverId, newText)
  }

  suspend fun deleteMessageByServerId(serverId: Int) {
    db.messageDao().deleteByServerId(serverId)
  }

  suspend fun claimPendingMessage(chatId: String, senderId: Int, text: String, serverId: Int) {
    db.messageDao().claimPendingMessage(chatId, senderId, text, serverId)
  }
}