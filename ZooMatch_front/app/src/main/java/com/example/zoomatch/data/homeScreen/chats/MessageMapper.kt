package com.example.zoomatch.data.homeScreen.chats

import com.example.zoomatch.data.db.MessageEntity
import org.json.JSONObject

object MessageMapper {

  fun fromServerToEntity(
    serverMessage: JSONObject,  // или твоя серверная модель
    currentUserId: Int
  ): MessageEntity {
    return MessageEntity(
      id = serverMessage.getInt("message_id"),
      chatId = generateChatId(currentUserId, serverMessage.getInt("sender_id")),
      senderId = serverMessage.getInt("sender_id"),
      receiverId = serverMessage.optInt("receiver_id", currentUserId),
      text = serverMessage.optString("text"),
      mediaUrl = if (serverMessage.optBoolean("has_media")) serverMessage.optString("media_url") else null,
      mediaType = serverMessage.optString("media_type"),
      isDelivered = true,
      isRead = serverMessage.optBoolean("is_read", false),
      createdAt = System.currentTimeMillis() // или парсить created_at
    )
  }

  private fun generateChatId(user1: Int, user2: Int): String {
    val ids = listOf(user1, user2).sorted()
    return "chat_${ids[0]}_${ids[1]}"
  }
}