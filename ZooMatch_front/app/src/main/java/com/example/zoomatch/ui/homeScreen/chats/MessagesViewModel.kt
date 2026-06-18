package com.example.zoomatch.ui.homeScreen.chats

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.db.CreateMessageRequest
import com.example.zoomatch.data.db.EditMessageRequest
import com.example.zoomatch.data.db.MessageEntity
import com.example.zoomatch.data.db.WebSocketManager
import com.example.zoomatch.data.db.ZooMatchApi
import com.example.zoomatch.data.homeScreen.chats.ChatsRemoteDataSource
import com.example.zoomatch.data.homeScreen.chats.CurrentUserProvider
import com.example.zoomatch.data.homeScreen.chats.FileAttachmentHelper
import com.example.zoomatch.data.homeScreen.chats.MessagesRepository
import com.example.zoomatch.data.homeScreen.chats.ProgressRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
  private val repository: MessagesRepository,
  private val remoteDataSource: ChatsRemoteDataSource,
  private val webSocketManager: WebSocketManager,
  private val currentUserProvider: CurrentUserProvider,
  private val fileHelper: FileAttachmentHelper,
  private val api: ZooMatchApi
) : ViewModel() {

  private val chatIdFlow = MutableStateFlow("")
  private var interlocutorId: Int = 0
  var currentUserId: Int = 0
    private set

  private val _editingMessageId = MutableStateFlow<Int?>(null)
  val editingMessageId: StateFlow<Int?> = _editingMessageId.asStateFlow()

  private val _editingText = MutableStateFlow("")
  val editingText: StateFlow<String> = _editingText.asStateFlow()

  private val _uploadProgress = MutableStateFlow<Map<Long, Int>>(emptyMap())
  val uploadProgress: StateFlow<Map<Long, Int>> = _uploadProgress.asStateFlow()

  val messages = chatIdFlow
    .flatMapLatest { chatId -> repository.getMessages(chatId) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setChat(chatId: String, interlocutorId: Int) {
    this.chatIdFlow.value = chatId
    this.interlocutorId = interlocutorId

    viewModelScope.launch {
      currentUserId = currentUserProvider.getCurrentUserId()
      loadInitialMessages()
      val token = currentUserProvider.getAccessToken()
      if (token != null && currentUserId != 0) {
        webSocketManager.connect(token, currentUserId, interlocutorId)
      }
    }
  }

  private suspend fun loadInitialMessages() {
    try {
      val token = currentUserProvider.getAccessToken() ?: return
      repository.deleteAllMessagesInChat(chatIdFlow.value)
      val response = api.getMessages("Bearer $token", interlocutorId)
      if (response.isSuccessful && response.body() != null) {
        val serverMessages = response.body()!!.results.reversed()
        val entities = serverMessages.map { serverMsg ->
          MessageEntity(
            id = serverMsg.id,
            chatId = chatIdFlow.value,
            senderId = serverMsg.sender?.id ?: 0,
            receiverId = serverMsg.receiver?.id ?: 0,
            text = serverMsg.text,
            mediaUrl = serverMsg.media,
            isDelivered = true,
            isRead = serverMsg.is_read,
            isPending = false,
            createdAt = parseServerTimeToLong(serverMsg.created_at)
          )
        }
        repository.insertAll(entities)
      }
    } catch (e: Exception) {
      Log.e("MessagesViewModel", "Ошибка истории: ${e.message}")
    }
  }

  fun editMessage(serverId: Int, newText: String) {
    viewModelScope.launch {
      try {
        val token = currentUserProvider.getAccessToken() ?: return@launch
        val response = api.editMessage("Bearer $token", serverId, EditMessageRequest(newText))
        if (response.isSuccessful) {
          repository.updateMessageText(serverId, newText)
        }
      } catch (e: Exception) {
        Log.e("MessagesViewModel", "Ошибка редактирования: ${e.message}")
      }
    }
  }

  fun deleteMessage(serverId: Int) {
    viewModelScope.launch {
      try {
        val token = currentUserProvider.getAccessToken() ?: return@launch
        val response = api.deleteMessage("Bearer $token", serverId)
        if (response.isSuccessful) {
          repository.deleteMessageByServerId(serverId)
        }
      } catch (e: Exception) {
        Log.e("MessagesViewModel", "Ошибка удаления: ${e.message}")
      }
    }
  }

  fun markAsRead(serverId: Int) {
    viewModelScope.launch {
      try {
        val token = currentUserProvider.getAccessToken() ?: return@launch
        api.markMessageAsRead("Bearer $token", serverId)
        repository.markAsRead(serverId.toLong(), currentUserId)
      } catch (_: Exception) { }
    }
  }

  fun startEditing(messageId: Int, currentText: String) {
    _editingMessageId.value = messageId
    _editingText.value = currentText
  }

  fun cancelEditing() {
    _editingMessageId.value = null
    _editingText.value = ""
  }

  fun sendTextMessage(text: String) {
    val editingId = _editingMessageId.value
    if (editingId != null) {
      editMessage(editingId, text)
      cancelEditing()
      return
    }
    viewModelScope.launch {
      val message = MessageEntity(
        chatId = chatIdFlow.value,
        senderId = currentUserId,
        receiverId = interlocutorId,
        text = text,
        isPending = true,
        createdAt = System.currentTimeMillis()
      )
      repository.insertMessage(message)
      webSocketManager.sendMessage(chatIdFlow.value, text, interlocutorId)
    }
  }

  fun sendFile(uri: Uri) {
    viewModelScope.launch {
      val fileInfo = fileHelper.copyFileToAppStorage(uri) ?: return@launch
      val localFile = File(fileInfo.path)

      val draftMessage = MessageEntity(
        chatId = chatIdFlow.value,
        senderId = currentUserId,
        receiverId = interlocutorId,
        text = fileInfo.originalName,
        mediaUrl = fileInfo.path,
        mediaType = fileInfo.fileType,
        isPending = true,
        createdAt = System.currentTimeMillis()
      )
      val localId = repository.insertMessage(draftMessage)

      try {
        val token = currentUserProvider.getAccessToken() ?: return@launch
        val authHeader = "Bearer $token"
        val baseUrl = "https://zoomatch.ru"

        val createResponse = api.createMessage(
          authHeader,
          CreateMessageRequest(receiverId = interlocutorId, text = fileInfo.originalName)
        )

        if (createResponse.isSuccessful && createResponse.body() != null) {
          val createdServerMsg = createResponse.body()!!
          val serverMessageId = createdServerMsg.id

          _uploadProgress.value = _uploadProgress.value + (localId to 0)

          val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

          val requestBody = localFile.asRequestBody(fileInfo.mimeType.toMediaTypeOrNull())
          val progressBody = ProgressRequestBody(requestBody) { progress ->
            _uploadProgress.value = _uploadProgress.value + (localId to progress)
          }

          val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("media", localFile.name, progressBody)
            .build()

          val uploadUrl = "$baseUrl/api/v1/messages/$serverMessageId/media/"
          val request = Request.Builder()
            .url(uploadUrl)
            .addHeader("Authorization", authHeader)
            .post(body)
            .build()

          val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
          }

          if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val mediaUrl = if (responseBody != null) {
              try {
                org.json.JSONObject(responseBody).optString("media", "")
              } catch (e: Exception) {
                ""
              }
            } else ""

            webSocketManager.sendMediaMessage(serverMessageId)

            val successMessage = draftMessage.copy(
              localId = localId,
              id = serverMessageId,
              text = fileInfo.originalName,
              mediaUrl = mediaUrl,
              isPending = false
            )
            repository.insertMessage(successMessage)
          }

          _uploadProgress.value = _uploadProgress.value - localId
        }
      } catch (e: Exception) {
        Log.e("MessagesViewModel", "Ошибка отправки файла: ${e.message}")
        _uploadProgress.value = _uploadProgress.value - localId
      }
    }
  }

  private fun parseServerTimeToLong(timeStr: String): Long {
    return try {
      val clean = timeStr.replace(Regex("\\.\\d+"), "")
      val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US)
      sdf.parse(clean)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
      try {
        val clean = timeStr.replace(Regex("\\.\\d+"), "")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sdf.parse(clean)?.time ?: System.currentTimeMillis()
      } catch (e2: Exception) {
        System.currentTimeMillis()
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    webSocketManager.disconnect()
  }
}