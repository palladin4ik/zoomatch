package com.example.zoomatch.ui.homeScreen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.homeScreen.profile.ProfileRepository
import com.example.zoomatch.data.homeScreen.profile.UserUI
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
  repository: ProfileRepository,
  userDao: UserDao
) : ViewModel() {
  val user = repository.userFlow
    .map {
      UserUI(
        avatar = it?.avatar.orEmpty(),
        firstname = it?.firstname.orEmpty(),
        lastname = it?.lastname.orEmpty(),
        geo = it?.location.orEmpty(),
        description = it?.status.orEmpty(),
        email = it?.email.orEmpty(),
        phone_number = it?.phone_number,
        organization = it?.organization
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserUI("", "", "", "", "", "", null, null)
    )

  val petCount = userDao.getPetCountForCurrentUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private val _openEditProfile = Channel<Unit>(Channel.CONFLATED)
  val openEditProfile = _openEditProfile.receiveAsFlow()
  fun onEditProfileClick() = viewModelScope.launch {
    _openEditProfile.send(Unit)
  }

  fun uploadAvatar(base64: String) {
    // TODO: upload avatar via API
  }
}
