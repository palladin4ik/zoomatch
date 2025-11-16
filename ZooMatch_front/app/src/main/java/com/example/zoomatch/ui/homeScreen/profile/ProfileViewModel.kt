package com.example.zoomatch.ui.homeScreen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.profile.ProfileRepository
import com.example.zoomatch.data.homeScreen.profile.UserUI
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
  repository: ProfileRepository
) : ViewModel() {
  val user = repository.userFlow
    .map {
      UserUI(
        it.avatar.orEmpty(),
        it.name,
        it.location.orEmpty(),
        it.status.orEmpty()
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserUI("", "Загрузка...", "", "")
    )
  private val _openEditProfile = Channel<Unit>(Channel.CONFLATED)
  val openEditProfile = _openEditProfile.receiveAsFlow()
  fun onEditProfileClick() = viewModelScope.launch {
    _openEditProfile.send(Unit)
  }
}