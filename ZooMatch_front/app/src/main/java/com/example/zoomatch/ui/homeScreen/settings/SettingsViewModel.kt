package com.example.zoomatch.ui.homeScreen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.settings.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val repository: SettingsRepository
) : ViewModel() {

  private val _logout = Channel<Unit>(Channel.CONFLATED)
  val logout = _logout.receiveAsFlow()

  private val _openPrivacy = Channel<Unit>(Channel.CONFLATED)
  val openPrivacy = _openPrivacy.receiveAsFlow()

  private val _openTerms = Channel<Unit>(Channel.CONFLATED)
  val openTerms = _openTerms.receiveAsFlow()

  private val _openApproveProfile = Channel<Unit>(Channel.CONFLATED)
  val openApproveProfile = _openApproveProfile.receiveAsFlow()

  private val _openEditPassword = Channel<Unit>(Channel.CONFLATED)
  val openEditPassword = _openEditPassword.receiveAsFlow()

  fun onLogoutClicked() {
    viewModelScope.launch {
      repository.logout()
      _logout.send(Unit)
    }
  }

  fun onOpenPrivacyClick() = viewModelScope.launch { _openPrivacy.send(Unit) }
  fun onOpenTermsClick() = viewModelScope.launch { _openTerms.send(Unit) }
  fun onOpenApproveProfileClick() = viewModelScope.launch { _openApproveProfile.send(Unit) }
  fun onOpenEditPasswordClick() = viewModelScope.launch { _openEditPassword.send(Unit) }
}