package com.example.zoomatch.ui.startScreen

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zoomatch.R
import com.example.zoomatch.data.startScreen.RegFormState
import com.example.zoomatch.data.startScreen.RegRepository
import com.example.zoomatch.data.startScreen.RegResult
import com.example.zoomatch.data.startScreen.Result

class RegViewModel(private val regRepository: RegRepository) : ViewModel() {
  private val _regForm = MutableLiveData<RegFormState>()
  val regFormState: LiveData<RegFormState> = _regForm
  private val _regResult = MutableLiveData<RegResult>()
  val regResult: LiveData<RegResult> = _regResult


  fun register(email: String, password: String, username: String) {
    val result = regRepository.register(username, password, username)
    _regResult.value = if (result is Result.Success) {
      RegResult(success = result.data)
    } else {
      RegResult(error = R.string.login_failed)
    }
  }


  fun regDataChanged(email: String, password: String, username: String) {
    if (!isEmailValid(email)) {
      _regForm.value = RegFormState(emailError = R.string.invalid_email)
    } else if (!isPasswordValid(password)) {
      _regForm.value = RegFormState(passwordError = R.string.invalid_password)
    } else if (!isUsernameValid(username)) {
      _regForm.value = RegFormState(usernameError = R.string.invalid_username)
    } else {
      _regForm.value = RegFormState(isDataValid = true)
    }
    val newState = RegFormState(
      emailError = if (email.isNotBlank() && !isEmailValid(email)) R.string.invalid_email else null,
      passwordError = if (password.isNotBlank() && !isPasswordValid(password)) R.string.invalid_password else null,
      usernameError = if (username.isNotBlank() && !isUsernameValid(username)) R.string.invalid_username else null,
      isDataValid = email.isNotBlank() && password.isNotBlank() && username.isNotBlank()
          && isEmailValid(email) && isPasswordValid(password) && isUsernameValid(username)
    )
    _regForm.value = newState
  }


  private fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }


  private fun isPasswordValid(password: String): Boolean {
    return password.length > 5
  }


  private fun isUsernameValid(username: String): Boolean {
    return username.isNotBlank()
  }
}