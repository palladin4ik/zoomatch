package com.example.zoomatch.ui.startScreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.startScreen.LoginDataSource
import com.example.zoomatch.data.startScreen.RegDataSource
import com.example.zoomatch.data.startScreen.RegRepository

class RegViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    val tokenManager = TokenManager(context)
    val db = AppDatabase.getDatabase(context)
    val repository = RegRepository(
      RegDataSource(),
      LoginDataSource(),
      tokenManager,
      db.userDao()
    )
    return RegViewModel(repository) as T
  }
}