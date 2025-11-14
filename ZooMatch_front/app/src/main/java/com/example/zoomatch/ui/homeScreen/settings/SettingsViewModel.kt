package com.example.zoomatch.ui.homeScreen.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.settings.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val repository: SettingsRepository
) : ViewModel() {

  private val _logoutEvent = MutableLiveData<Unit>()
  val logoutEvent: LiveData<Unit> = _logoutEvent

  fun onLogoutClicked() {
    viewModelScope.launch {
      repository.logout()
      _logoutEvent.postValue(Unit)  // ← одноразовое событие
    }
  }
}