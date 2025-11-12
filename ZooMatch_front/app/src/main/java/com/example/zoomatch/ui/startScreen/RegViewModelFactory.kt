package com.example.zoomatch.ui.startScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.data.startScreen.RegDataSource
import com.example.zoomatch.data.startScreen.RegRepository

class RegViewModelFactory : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(RegViewModel::class.java)) {
      return RegViewModel(
        regRepository = RegRepository(
          dataSource = RegDataSource()
        )
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}