package com.example.zoomatch.data.homeScreen.chats

import com.example.zoomatch.data.db.MessageEntity
import com.example.zoomatch.data.db.ZooMatchApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatsRemoteDataSource @Inject constructor(
  private val api: ZooMatchApi,
  private val currentUserProvider: CurrentUserProvider
) {

  fun getAllChats(currentUserId: Int): Flow<List<ChatItem>> = flow {
    try {
      val token = currentUserProvider.getAccessToken() ?: return@flow
      val response = api.getChats("Bearer $token")

      val chats = response.body()?.map { chatResponse ->
        ChatItem(
          chatId = "chat_${minOf(currentUserId, chatResponse.user.id)}_${maxOf(currentUserId, chatResponse.user.id)}",
          interlocutorId = chatResponse.user.id,
          name = chatResponse.user.displayName,
          avatar = chatResponse.user.avatar,
          lastMessage = chatResponse.last_message_text,
          lastMessageTime = null,
          unreadCount = chatResponse.unread_count ?: 0
        )
      } ?: emptyList()

      emit(chats)
    } catch (e: Exception) {
      emit(emptyList())
    }
  }

  suspend fun loadMessagesFromServer(chatId: String, receiverId: Int): List<MessageEntity> {
    return emptyList() // позже реализуем
  }
}
