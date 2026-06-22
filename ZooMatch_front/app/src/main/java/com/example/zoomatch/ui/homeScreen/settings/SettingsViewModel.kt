package com.example.zoomatch.ui.homeScreen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.settings.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val repository: SettingsRepository
) : ViewModel() {

  private val _logout = Channel<Unit>(Channel.CONFLATED)
  val logout = _logout.receiveAsFlow()


  fun logoutClick() = viewModelScope.launch {
    repository.logout()
    _logout.send(Unit)
  }
  private val _delete = Channel<Result<String>>(Channel.CONFLATED)
  val delete = _delete.receiveAsFlow()

  fun deleteClick() = viewModelScope.launch {
    val result = repository.delete()
    _delete.send(result)
  }
}