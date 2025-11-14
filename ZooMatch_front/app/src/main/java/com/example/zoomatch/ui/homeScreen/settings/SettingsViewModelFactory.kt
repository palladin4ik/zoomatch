package com.example.zoomatch.ui.homeScreen.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.homeScreen.settings.SettingsRepository

class SettingsViewModelFactory(
  private val application: Application
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    val tokenManager = TokenManager(application)
    val db = AppDatabase.getDatabase(application)
    val repo = SettingsRepository(tokenManager, db.userDao())
    return SettingsViewModel(repo) as T
  }
}