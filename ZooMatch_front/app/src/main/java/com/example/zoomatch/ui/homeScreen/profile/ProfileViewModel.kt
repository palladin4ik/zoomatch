package com.example.zoomatch.ui.homeScreen.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.ZooMatchApi
import com.example.zoomatch.data.homeScreen.profile.ProfileRepository
import com.example.zoomatch.data.homeScreen.profile.UserUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileViewModel(
  repository: ProfileRepository,
  userDao: UserDao,
  private val api: ZooMatchApi,
  private val tokenManager: TokenManager,
  private val application: Application
) : ViewModel() {
  val user = repository.userFlow
    .map {
      UserUI(
        avatar = it?.avatar.orEmpty(),
        firstname = it?.firstname.orEmpty(),
        lastname = it?.lastname.orEmpty(),
        geo = it?.location.orEmpty(),
        description = it?.status.orEmpty(),
        email = it?.email.orEmpty(),
        phone_number = it?.phone_number,
        organization = it?.organization
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserUI("", "", "", "", "", "", null, null)
    )

  val petCount = userDao.getPetCountForCurrentUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private val _matchCount = MutableStateFlow(0)
  val matchCount: StateFlow<Int> = _matchCount

  private val _avatarUploadResult = Channel<Boolean>(Channel.CONFLATED)
  val avatarUploadResult = _avatarUploadResult.receiveAsFlow()

  init {
    fetchMatchCount()
  }

  fun fetchMatchCount() {
    viewModelScope.launch {
      try {
        val token = tokenManager.getAccessToken() ?: return@launch
        val response = api.getMatchesList("Bearer $token")
        if (response.isSuccessful) {
          val matches = response.body() ?: emptyList()
          _matchCount.value = matches.count { it.status == 1 }
        }
      } catch (_: Exception) {
        _matchCount.value = 0
      }
    }
  }

  fun uploadAvatar(uri: Uri) {
    viewModelScope.launch {
      try {
        val token = tokenManager.getAccessToken() ?: return@launch
        val file = withContext(Dispatchers.IO) {
          val inputStream = application.contentResolver.openInputStream(uri) ?: return@withContext null
          val file = File(application.cacheDir, "avatar_upload.jpg")
          file.outputStream().use { output -> inputStream.copyTo(output) }
          file
        } ?: return@launch

        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("avatar", file.name, requestBody)
        val response = api.uploadUserAvatar("Bearer $token", part)
        _avatarUploadResult.send(response.isSuccessful)
      } catch (_: Exception) {
        _avatarUploadResult.send(false)
      }
    }
  }

  private val _openEditProfile = Channel<Unit>(Channel.CONFLATED)
  val openEditProfile = _openEditProfile.receiveAsFlow()
  fun onEditProfileClick() = viewModelScope.launch {
    _openEditProfile.send(Unit)
  }
}
