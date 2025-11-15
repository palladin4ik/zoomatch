package com.example.zoomatch.ui.homeScreen.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.profile.ProfileRepository
import com.example.zoomatch.data.homeScreen.profile.userUI
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProfileViewModel(val repository: ProfileRepository) : ViewModel() {

  private val _userCard = MutableLiveData<Any>()
  val userCard: LiveData<Any> = _userCard
  private lateinit var _user : MutableLiveData<userUI>
  val user: LiveData<userUI> = _user

  private val _openEditProfile = Channel<Unit>(Channel.CONFLATED)
  val openEditProfile = _openEditProfile.receiveAsFlow()

  fun onEditProfileClick() = viewModelScope.launch { _openEditProfile.send(Unit) }

  suspend fun getUser() {
    _user = repository.getUser()
  }

}