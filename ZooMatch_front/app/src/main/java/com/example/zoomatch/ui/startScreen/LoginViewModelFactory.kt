package com.example.zoomatch.ui.startScreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.startScreen.LoginDataSource
import com.example.zoomatch.data.startScreen.LoginRepository

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    val tokenManager = TokenManager(context)
    val db = AppDatabase.getDatabase(context)
    val repository = LoginRepository(
      LoginDataSource(),
      tokenManager,
      db.userDao()
    )
    return LoginViewModel(repository) as T
  }
}