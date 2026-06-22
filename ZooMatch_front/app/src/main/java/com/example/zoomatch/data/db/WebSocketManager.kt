package com.example.zoomatch.data.db

import android.util.Log
import com.example.zoomatch.data.homeScreen.chats.MessagesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
  private val messagesRepository: MessagesRepository
) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.MILLISECONDS)
    .build()

  private var webSocket: WebSocket? = null
  private var currentUserId: Int = 0
  private var currentInterlocutorId: Int = 0
  private var currentToken: String? = null

  private var reconnectAttempts = 0
  private val maxReconnectDelay = 16000L

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  fun connect(token: String, myUserId: Int, interlocutorId: Int) {
    currentUserId = myUserId
    currentInterlocutorId = interlocutorId
    currentToken = token

    val url = "wss://zoomatch.ru/ws/chats/$interlocutorId/?token=$token"
    val request = Request.Builder().url(url).build()

    webSocket = client.newWebSocket(request, object : WebSocketListener() {

      override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Соединение успешно установлено")
        reconnectAttempts = 0
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        handleIncomingMessage(text)
      }

      override fun onMessage(webSocket: WebSocket, bytes: ByteString) {}

      override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "Соединение закрывается сервером: $code - $reason")
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "Ошибка соединения: ${t.message}")
        scheduleReconnect()
      }
    })
  }

  private fun handleIncomingMessage(text: String) {
    try {
      val json = JSONObject(text)
      val type = json.optString("type")

      when (type) {
        "message" -> {
          val serverId = json.getInt("message_id")
          val senderId = json.getInt("sender_id")
          val textContent = json.optString("text", "")
          val hasMedia = json.optBoolean("has_media", false)

          val createdAtStr = json.optString("created_at")
          val createdAtLong = parseServerTime(createdAtStr)

          val chatId = "chat_${minOf(currentUserId, senderId)}_${maxOf(currentUserId, senderId)}"

          val message = MessageEntity(
            id = serverId,
            chatId = chatId,
            senderId = senderId,
            receiverId = currentUserId,
            text = textContent,
            mediaUrl = if (hasMedia) json.optString("media_url", null) else null,
            isDelivered = true,
            isRead = false,
            isPending = false,
            createdAt = createdAtLong
          )

          scope.launch {
            if (senderId == currentUserId) {
              messagesRepository.claimPendingMessage(chatId, senderId, textContent, serverId)
            } else {
              messagesRepository.insertMessage(message)
            }
            sendDeliveredStatus(serverId)
          }
        }
        "read" -> {
          val messageId = json.getLong("message_id")
          scope.launch {
            messagesRepository.markAsRead(messageId, currentUserId)
          }
        }
        "delivered" -> {
          val messageId = json.getLong("message_id")
          scope.launch {
            messagesRepository.markAsDelivered(messageId)
          }
        }
      }
    } catch (e: Exception) {
      Log.e("WebSocket", "Ошибка парсинга: ${e.message}")
    }
  }

  fun sendMessage(chatId: String, text: String, receiverId: Int) {
    val message = JSONObject().apply {
      put("type", "message")
      put("text", text)
      put("receiver_id", receiverId)
    }
    webSocket?.send(message.toString())
  }

  fun sendMediaMessage(messageId: Int) {
    val message = JSONObject().apply {
      put("type", "media_message")
      put("message_id", messageId)
    }
    webSocket?.send(message.toString())
  }

  fun sendReadStatus(messageId: Int) {
    val message = JSONObject().apply {
      put("type", "read")
      put("message_id", messageId)
    }
    webSocket?.send(message.toString())
  }

  fun sendDeliveredStatus(messageId: Int) {
    val message = JSONObject().apply {
      put("type", "delivered")
      put("message_id", messageId)
    }
    webSocket?.send(message.toString())
  }

  private fun scheduleReconnect() {
    val delay = minOf(1000L * (1 shl reconnectAttempts), maxReconnectDelay)
    reconnectAttempts++
    Log.d("WebSocket", "Повторная попытка соединения через $delay мс")
    scope.launch {
      delay(delay)
      reconnect()
    }
  }

  private fun reconnect() {
    val token = currentToken
    if (token != null && currentUserId != 0 && currentInterlocutorId != 0) {
      connect(token, currentUserId, currentInterlocutorId)
    }
  }

  fun disconnect() {
    webSocket?.close(1000, "User disconnected")
    webSocket = null
    currentToken = null
    currentUserId = 0
    currentInterlocutorId = 0
  }

  private fun parseServerTime(timeStr: String?): Long {
    if (timeStr.isNullOrEmpty()) return System.currentTimeMillis()
    return try {
      val clean = timeStr.replace(Regex("\\.\\d+"), "")
      val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
      sdf.parse(clean)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
      try {
        val clean = timeStr.replace(Regex("\\.\\d+"), "")
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sdf.parse(clean)?.time ?: System.currentTimeMillis()
      } catch (e2: Exception) {
        System.currentTimeMillis()
      }
    }
  }
}