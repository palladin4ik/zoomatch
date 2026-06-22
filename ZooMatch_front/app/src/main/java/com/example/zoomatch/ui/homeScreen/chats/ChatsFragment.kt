package com.example.zoomatch.ui.homeScreen.chats

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.chats.ChatItem
import com.example.zoomatch.databinding.FragmentChatsBinding
import com.example.zoomatch.databinding.ItemChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatsFragment : Fragment() {

  private var _binding: FragmentChatsBinding? = null
  private val binding get() = _binding!!
  private val viewModel: ChatsViewModel by viewModels()
  private lateinit var adapter: ChatsAdapter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentChatsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecyclerView()
    observeViewModel()
  }

  private fun setupRecyclerView() {
    adapter = ChatsAdapter { chatItem ->
      val args = Bundle().apply {
        putString("chatId", chatItem.chatId)
        putInt("interlocutorId", chatItem.interlocutorId)
        putString("interlocutorName", chatItem.name)
      }
      findNavController().navigate(R.id.messagesFragment, args)
    }
    binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
    binding.rvChats.adapter = adapter
  }

  private fun observeViewModel() {
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.chats.collect { chats ->
          adapter.submitList(chats)
          if (chats.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvChats.visibility = View.GONE
          } else {
            binding.emptyState.visibility = View.GONE
            binding.rvChats.visibility = View.VISIBLE
          }
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

class ChatsAdapter(
  private val onChatClick: (ChatItem) -> Unit
) : ListAdapter<ChatItem, ChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
    val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ChatViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(chat: ChatItem) {
      binding.tvName.text = chat.name
      binding.tvLastMessage.text = formatLastMessage(chat.lastMessage)
      binding.tvTime.text = formatTime(chat.lastMessageTime)
      binding.tvUnreadCount.text = if (chat.unreadCount > 0) chat.unreadCount.toString() else ""
      binding.tvUnreadCount.visibility = if (chat.unreadCount > 0) View.VISIBLE else View.GONE

      val avatarUrl = when {
        chat.avatar.isNullOrBlank() -> null
        chat.avatar.startsWith("http") -> chat.avatar
        else -> "https://zoomatch.ru${chat.avatar}"
      }
      Log.d("CHAT_AVATAR", "bind: chat=${chat.name}, avatar_raw='${chat.avatar}', url=$avatarUrl")
      Glide.with(binding.root.context)
        .load(avatarUrl)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.ic_paw_print_light)
        .circleCrop()
        .listener(object : RequestListener<Drawable> {
          override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
            Log.e("CHAT_AVATAR", "FAILED url=$avatarUrl, chat=${chat.name}, avatar_raw=${chat.avatar}", e)
            return false
          }
          override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
            Log.d("CHAT_AVATAR", "LOADED url=$avatarUrl, chat=${chat.name}")
            return false
          }
        })
        .into(binding.ivAvatar)

      binding.root.setOnClickListener { onChatClick(chat) }
    }

    private fun formatLastMessage(text: String?): String {
      if (text.isNullOrBlank()) return "Нет сообщений"
      val ext = text.substringAfterLast('.', "").lowercase()
      if (ext in IMAGE_EXTENSIONS) return "Изображение"
      return text
    }

    private fun formatTime(timestamp: Long?): String {
      return timestamp?.let { DateFormat.format("HH:mm", it).toString() } ?: ""
    }
  }

  class ChatDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.chatId == newItem.chatId
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem == newItem
  }

  companion object {
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
  }
}