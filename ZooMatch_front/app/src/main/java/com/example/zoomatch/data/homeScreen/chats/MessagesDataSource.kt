package com.example.zoomatch.data.homeScreen.chats

import com.example.zoomatch.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow

interface MessagesDataSource {
  fun getMessages(chatId: String): Flow<List<MessageEntity>>
  suspend fun insertMessage(message: MessageEntity): Long
  suspend fun markAsRead(messageId: Long, currentUserId: Int)
  suspend fun markAsDelivered(messageId: Long)
  suspend fun deleteAllMessagesInChat(chatId: String)
}