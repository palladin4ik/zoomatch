package com.example.zoomatch.ui.homeScreen.profile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.R
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.profile.EditProfileFormState
import com.example.zoomatch.data.homeScreen.profile.ProfileRepository
import com.example.zoomatch.data.homeScreen.profile.UserEditUI
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class EditProfileViewModel(
  private val repository: ProfileRepository
) : ViewModel() {
  val user = repository.userFlow
    .map {
      UserEditUI(
        avatar = it?.avatar,
        firstname = it?.firstname.orEmpty(),
        lastname = it?.lastname.orEmpty(),
        email = it?.email.orEmpty(),
        description = it?.status.orEmpty(),
        phone_number = it?.phone_number.orEmpty(),
        organization = it?.organization
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserEditUI(
        avatar = null,
        firstname = "",
        lastname = "",
        email = "",
        description = "",
        phone_number = "",
        organization = null
      )
    )
  private val _avatarBase64 = MutableStateFlow<String?>(null)
  val avatarBase64: StateFlow<String?> = _avatarBase64
  fun setAvatarBase64(base64: String) {
    _avatarBase64.value = base64
  }

  private val _formState = MutableStateFlow(EditProfileFormState())
  val formState: StateFlow<EditProfileFormState> = _formState

  fun validate(firstname: String, lastname: String, email: String, phone: String) {
    val firstnameError = if (firstname.isBlank()) R.string.invalid_name else null
    val lastnameError = if (lastname.isBlank()) R.string.invalid_name else null
    val emailError = if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
      R.string.invalid_email else null
    val phoneError = if (phone.isNotBlank() && phone.length < 12) R.string.invalid_phone else null

    val isValid = firstnameError == null && lastnameError == null && emailError == null && phoneError == null

    _formState.value = EditProfileFormState(
      firstnameError = firstnameError,
      lastnameError = lastnameError,
      emailError = emailError,
      phoneError = phoneError,
      isDataValid = isValid
    )
  }

  private val _updateResult = Channel<Result<String>>(Channel.CONFLATED)
  val updateResult = _updateResult.receiveAsFlow()
  fun update(
    firstname: String,
    lastname: String,
    description: String,
    email: String,
    phoneNumber: String
  ) {
    viewModelScope.launch {
      val result = repository.updateProfile(
        firstname, lastname, description, email, phoneNumber
      )
      _updateResult.send(result)
    }
  }
}
