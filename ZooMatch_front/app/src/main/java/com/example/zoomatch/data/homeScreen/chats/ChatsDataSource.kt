package com.example.zoomatch.data.homeScreen.chats

import com.example.zoomatch.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatsDataSource {
  fun getAllChats(currentUserId: Int): Flow<List<ChatItem>>
  suspend fun getLastMessageForChat(chatId: String): MessageEntity?
}