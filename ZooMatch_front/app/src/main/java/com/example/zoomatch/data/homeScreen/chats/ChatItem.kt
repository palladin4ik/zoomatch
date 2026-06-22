package com.example.zoomatch.data.homeScreen.chats

data class ChatItem(
  val chatId: String,
  val interlocutorId: Int,
  val name: String,
  val avatar: String?,
  val lastMessage: String?,
  val lastMessageTime: Long?,
  val unreadCount: Int = 0
)

data class ChatResponse(
  val user: SimpleUser,
  val last_message_text: String?,
  val unread_count: Int?
)

data class SimpleUser(
  val id: Int,
  val firstname: String?,
  val lastname: String?,
  val avatar: String?
) {
  val displayName: String
    get() = listOfNotNull(firstname, lastname).joinToString(" ").ifEmpty { "User $id" }
}
