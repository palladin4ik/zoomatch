package com.example.zoomatch.data.homeScreen.profile

import androidx.lifecycle.MutableLiveData
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.UserEntity

class ProfileRepository(
  private val userDao: UserDao
) {

  suspend fun getUser(): MutableLiveData<userUI>{
    val user = userDao.getCurrentUser()
    val userUI = userUI(
      user?.avatar.toString(),
      user?.name.toString(),
      user?.location.toString(),
      user?.description.toString())
    return userUI as MutableLiveData<userUI>
  }

}