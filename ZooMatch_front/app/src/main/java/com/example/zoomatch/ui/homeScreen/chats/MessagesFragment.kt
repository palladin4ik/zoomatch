package com.example.zoomatch.ui.homeScreen.chats

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.data.db.MessageEntity
import com.example.zoomatch.databinding.FragmentMessagesBinding
import com.example.zoomatch.databinding.ItemMessageIncomingBinding
import com.example.zoomatch.databinding.ItemMessageOutgoingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MessagesFragment : Fragment() {

  private var _binding: FragmentMessagesBinding? = null
  private val binding get() = _binding!!
  private val viewModel: MessagesViewModel by viewModels()
  private lateinit var adapter: MessagesAdapter

  private var chatId: String = ""
  private var interlocutorId: Int = 0
  private var interlocutorName: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      chatId = it.getString("chatId") ?: ""
      interlocutorId = it.getInt("interlocutorId")
      interlocutorName = it.getString("interlocutorName") ?: ""
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentMessagesBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.tvUserName.text = interlocutorName
    binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
    viewModel.setChat(chatId, interlocutorId)

    setupRecyclerView()
    setupListeners()
    observeViewModel()
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    @Suppress("DEPRECATION")
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
      data?.data?.let { uri ->
        viewModel.sendFile(uri)
      }
    }
  }

  private fun setupRecyclerView() {
    adapter = MessagesAdapter(
      currentUserIdProvider = { viewModel.currentUserId },
      baseUrl = "https://zoomatch.ru",
      onLongClick = { message, isOwn, view -> showPopupMenu(message, isOwn, view) },
      onFileClick = { message -> downloadAndOpenFile(message) }
    )
    binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
      stackFromEnd = true
    }
    binding.rvMessages.adapter = adapter
  }

  private fun setupListeners() {
    binding.btnSend.setOnClickListener {
      val text = binding.etMessage.text?.toString()?.trim()
      if (!text.isNullOrEmpty()) {
        viewModel.sendTextMessage(text)
        binding.etMessage.text?.clear()
      }
    }

    binding.btnAttachment.setOnClickListener {
      if (viewModel.editingMessageId.value != null) {
        Toast.makeText(requireContext(), "Завершите редактирование", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
        addCategory(Intent.CATEGORY_OPENABLE)
      }
      @Suppress("DEPRECATION")
      startActivityForResult(intent, 101)
    }

    binding.btnCancelEdit.setOnClickListener {
      viewModel.cancelEditing()
      binding.etMessage.text?.clear()
    }
  }

  private fun showPopupMenu(message: MessageEntity, isOwn: Boolean, anchorView: View) {
    val popup = PopupMenu(requireContext(), anchorView)
    if (isOwn && !message.isPending) {
      popup.menu.add(0, 1, 0, "Редактировать")
    }
    popup.menu.add(0, 2, if (isOwn) 1 else 0, "Удалить")
    popup.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        1 -> {
          viewModel.startEditing(message.id ?: return@setOnMenuItemClickListener true, message.text ?: "")
          binding.etMessage.setText(message.text ?: "")
          binding.etMessage.setSelection(message.text?.length ?: 0)
          binding.etMessage.requestFocus()
          true
        }
        2 -> {
          if (message.isPending) {
            Toast.makeText(requireContext(), "Сообщение ещё не отправлено", Toast.LENGTH_SHORT).show()
            return@setOnMenuItemClickListener true
          }
          val serverId = message.id
          if (serverId == null) {
            Toast.makeText(requireContext(), "Сообщение ещё не отправлено", Toast.LENGTH_SHORT).show()
            return@setOnMenuItemClickListener true
          }
          viewModel.deleteMessage(serverId)
          true
        }
        else -> false
      }
    }
    popup.show()
  }

  private fun downloadAndOpenFile(message: MessageEntity) {
    val mediaUrl = message.mediaUrl ?: return
    val url = if (mediaUrl.startsWith("http")) mediaUrl else "https://zoomatch.ru$mediaUrl"
    val displayName = message.text?.takeIf { it.isNotBlank() }
      ?: mediaUrl.substringAfterLast('/').substringBefore('?')

    Toast.makeText(requireContext(), "Загрузка файла...", Toast.LENGTH_SHORT).show()

    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val file = withContext(Dispatchers.IO) {
          val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
          val request = okhttp3.Request.Builder().url(url).build()
          val response = client.newCall(request).execute()

          if (!response.isSuccessful) {
            withContext(Dispatchers.Main) {
              Toast.makeText(requireContext(), "Ошибка загрузки файла", Toast.LENGTH_SHORT).show()
            }
            return@withContext null
          }

          val tempFile = java.io.File(requireContext().cacheDir, displayName)
          response.body?.byteStream()?.use { input ->
            tempFile.outputStream().use { output ->
              input.copyTo(output)
            }
          }
          tempFile
        } ?: return@launch

        val uri = androidx.core.content.FileProvider.getUriForFile(
          requireContext(),
          "${requireContext().packageName}.fileprovider",
          file
        )

        val ext = displayName.substringAfterLast('.', "").lowercase()
        val mimeType = MimeType_MAP[ext] ?: android.webkit.MimeTypeMap.getSingleton()
          .getMimeTypeFromExtension(ext) ?: "*/*"

        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
          setDataAndType(uri, mimeType)
          addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
      } catch (e: Exception) {
        Log.e("MessagesFragment", "Ошибка открытия файла: ${e.message}", e)
        Toast.makeText(requireContext(), "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun observeViewModel() {
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.messages.collect { messages ->
            adapter.submitList(messages) {
              if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
                messages
                  .filter { it.receiverId == viewModel.currentUserId && !it.isRead && it.id != null }
                  .forEach { msg -> viewModel.markAsRead(msg.id!!) }
              }
            }
          }
        }
        launch {
          viewModel.editingMessageId.collect { editingId ->
            if (editingId != null) {
              binding.editingBar.visibility = View.VISIBLE
            } else {
              binding.editingBar.visibility = View.GONE
              binding.etMessage.text?.clear()
            }
          }
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private val MimeType_MAP = mapOf(
      "jpg" to "image/jpeg",
      "jpeg" to "image/jpeg",
      "png" to "image/png",
      "webp" to "image/webp",
      "gif" to "image/gif",
      "mp4" to "video/mp4",
      "mov" to "video/quicktime",
      "pdf" to "application/pdf",
      "mp3" to "audio/mpeg",
      "ogg" to "audio/ogg",
      "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      "xls" to "application/vnd.ms-excel",
      "doc" to "application/msword",
      "ppt" to "application/vnd.ms-powerpoint",
      "zip" to "application/zip",
      "rar" to "application/x-rar-compressed",
      "txt" to "text/plain"
    )
  }
}

class MessagesAdapter(
  private val currentUserIdProvider: () -> Int,
  private val baseUrl: String,
  private val onLongClick: (MessageEntity, Boolean, View) -> Unit,
  private val onFileClick: (MessageEntity) -> Unit
) : ListAdapter<MessageEntity, RecyclerView.ViewHolder>(MessageDiffCallback()) {

  override fun getItemViewType(position: Int): Int {
    val message = getItem(position)
    return if (message.senderId == currentUserIdProvider()) 1 else 0
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == 1) {
      val binding = ItemMessageOutgoingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
      OutgoingViewHolder(binding)
    } else {
      val binding = ItemMessageIncomingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
      IncomingViewHolder(binding)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val message = getItem(position)
    if (holder is OutgoingViewHolder) holder.bind(message)
    else if (holder is IncomingViewHolder) holder.bind(message)
  }

  inner class OutgoingViewHolder(private val binding: ItemMessageOutgoingBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: MessageEntity) {
      val mediaUrl = message.mediaUrl
      val isImage = isImageUrl(mediaUrl)
      val isFile = !mediaUrl.isNullOrBlank() && !isImage
      val isUploading = message.isPending && isFile

      if (!mediaUrl.isNullOrBlank() && isImage) {
        binding.tvMessage.visibility = View.GONE
        binding.fileUploadContainer.visibility = View.GONE
        binding.ivMedia.visibility = View.VISIBLE
        val url = if (mediaUrl.startsWith("http")) mediaUrl else "$baseUrl$mediaUrl"
        Glide.with(binding.root.context)
          .load(url)
          .placeholder(R.drawable.test_avatar)
          .centerCrop()
          .into(binding.ivMedia)
        binding.root.setOnClickListener(null)
      } else if (isFile) {
        binding.tvMessage.visibility = View.GONE
        binding.ivMedia.visibility = View.GONE
        binding.fileUploadContainer.visibility = View.VISIBLE
        val fileName = mediaUrl.substringAfterLast('/').substringBefore('?')
        binding.tvFileName.text = message.text ?: fileName
        binding.root.setOnClickListener { onFileClick(message) }

        if (isUploading) {
          binding.progressCircular.visibility = View.VISIBLE
        } else {
          binding.progressCircular.visibility = View.GONE
        }
      } else {
        binding.tvMessage.visibility = View.VISIBLE
        binding.fileUploadContainer.visibility = View.GONE
        binding.ivMedia.visibility = View.GONE
        binding.tvMessage.text = message.text ?: ""
        binding.tvMessage.setTextColor(Color.WHITE)
        binding.root.setOnClickListener(null)
      }

      binding.tvTime.text = DateFormat.format("HH:mm", message.createdAt)
      binding.ivStatus.alpha = if (message.isPending) 0.5f else 1.0f

      val isOwn = message.senderId == currentUserIdProvider()
      binding.root.setOnLongClickListener {
        onLongClick(message, isOwn, it)
        true
      }
    }
  }

  inner class IncomingViewHolder(private val binding: ItemMessageIncomingBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: MessageEntity) {
      val mediaUrl = message.mediaUrl
      val isImage = isImageUrl(mediaUrl)

      if (!mediaUrl.isNullOrBlank() && isImage) {
        binding.tvMessage.visibility = View.GONE
        binding.ivMedia.visibility = View.VISIBLE
        val url = if (mediaUrl.startsWith("http")) mediaUrl else "$baseUrl$mediaUrl"
        Glide.with(binding.root.context)
          .load(url)
          .placeholder(R.drawable.test_avatar)
          .centerCrop()
          .into(binding.ivMedia)
        binding.root.setOnClickListener(null)
      } else if (!mediaUrl.isNullOrBlank()) {
        binding.tvMessage.visibility = View.VISIBLE
        binding.ivMedia.visibility = View.GONE
        val fileName = mediaUrl.substringAfterLast('/').substringBefore('?')
        binding.tvMessage.text = message.text ?: fileName
        binding.tvMessage.setTextColor(itemView.context.getColor(R.color.purple_primary))
        binding.root.setOnClickListener { onFileClick(message) }
      } else {
        binding.tvMessage.visibility = View.VISIBLE
        binding.ivMedia.visibility = View.GONE
        binding.tvMessage.text = message.text ?: ""
        binding.root.setOnClickListener(null)
      }

      binding.tvTime.text = DateFormat.format("HH:mm", message.createdAt)

      val isOwn = message.senderId == currentUserIdProvider()
      binding.root.setOnLongClickListener {
        onLongClick(message, isOwn, it)
        true
      }
    }
  }

  class MessageDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
    override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
      return if (oldItem.id != null && newItem.id != null) {
        oldItem.id == newItem.id
      } else {
        oldItem.localId == newItem.localId
      }
    }
    override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
  }

  companion object {
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    fun isImageUrl(url: String?): Boolean {
      if (url.isNullOrBlank()) return false
      val ext = url.substringAfterLast('.').substringBefore('/').substringBefore('?').lowercase()
      return ext in IMAGE_EXTENSIONS
    }
  }
}
