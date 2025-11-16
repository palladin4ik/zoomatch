package com.example.zoomatch.ui.homeScreen.settings.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.R
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.settings.EditPassFormState
import com.example.zoomatch.data.homeScreen.settings.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EditPassViewModel(
  private val repository: SettingsRepository
) : ViewModel() {
  private val _formState = MutableStateFlow(EditPassFormState())
  val formState: StateFlow<EditPassFormState> = _formState

  fun validate(oldPass: String, newPass: String, confPass: String) {
    _formState.value = EditPassFormState(
      oldPassError = null,
      passError = null,
      passConfError = null,
      isDataValid = false
    )
    val oldPassError =
      if (oldPass.isNotBlank() && oldPass.length < 6) R.string.invalid_password else null
    val passError = if (newPass.isNotBlank() && newPass.length < 6) R.string.invalid_password else null
    val passConfError =
      if (confPass.isNotBlank() && confPass.length < 6 || (confPass != newPass)) R.string.invalid_password2 else null

    val isAnyBlank = oldPass.isBlank() || newPass.isBlank() || confPass.isBlank()

    val isValid = oldPassError == null && passError == null && passConfError == null && !isAnyBlank

    _formState.value = EditPassFormState(
      oldPassError = oldPassError,
      passError = passError,
      passConfError = passConfError,
      isDataValid = isValid
    )
  }
  private val _updateResult = Channel<Result<String>>(Channel.CONFLATED)
  val updateResult = _updateResult.receiveAsFlow()
  fun updatePass(old_pass: String, new_pass: String){
    viewModelScope.launch {
      val result = repository.updatePass(
        old_pass, new_pass
      )
      _updateResult.send(result)
    }
  }

}