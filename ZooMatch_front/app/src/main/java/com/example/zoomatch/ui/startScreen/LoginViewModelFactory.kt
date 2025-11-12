package com.example.zoomatch.ui.startScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.data.startScreen.LoginDataSource
import com.example.zoomatch.data.startScreen.LoginRepository

class LoginViewModelFactory : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
      return LoginViewModel(
        loginRepository = LoginRepository(
          dataSource = LoginDataSource()
        )
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}