package com.example.zoomatch.ui.homeScreen

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.Network
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.homeScreen.profile.ProfileRepository
import com.example.zoomatch.data.homeScreen.settings.SettingsRepository
import com.example.zoomatch.ui.homeScreen.profile.ProfileViewModel
import com.example.zoomatch.ui.homeScreen.settings.SettingsViewModel

class HomeViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
  private val tokenManager = TokenManager(app)
  private val db = AppDatabase.getDatabase(app)
  private val api = Network.zooMatchApi

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when {
      modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
        val repo = ProfileRepository(db.userDao())
        ProfileViewModel(repo) as T
      }
      modelClass.isAssignableFrom(SettingsViewModel::class.java) ->{
        val repo = SettingsRepository(tokenManager, db.userDao())
        SettingsViewModel(repo) as T
      }
      else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
