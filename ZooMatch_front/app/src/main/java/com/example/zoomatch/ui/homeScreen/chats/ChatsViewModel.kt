package com.example.zoomatch.ui.homeScreen.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.chats.ChatsRepository
import com.example.zoomatch.data.homeScreen.chats.CurrentUserProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
  private val repository: ChatsRepository,
  private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

  private val currentUserIdFlow = MutableStateFlow<Int?>(null)

  val chats = currentUserIdFlow
    .filterNotNull()
    .flatMapLatest { userId ->
      repository.getAllChats(userId)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      val id = currentUserProvider.getCurrentUserId()
      currentUserIdFlow.value = id
      refreshChats()
    }
  }

  fun refreshChats() {
    viewModelScope.launch {
      currentUserIdFlow.value?.let { userId ->
        repository.fetchChatsFromServer(userId)
      }
    }
  }
}