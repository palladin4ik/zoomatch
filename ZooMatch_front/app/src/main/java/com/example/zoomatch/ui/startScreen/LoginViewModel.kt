package com.example.zoomatch.ui.startScreen

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.R
import com.example.zoomatch.data.startScreen.LoggedInUserView
import com.example.zoomatch.data.startScreen.LoginFormState
import com.example.zoomatch.data.startScreen.LoginRepository
import com.example.zoomatch.data.startScreen.LoginResult
import com.example.zoomatch.data.startScreen.Result
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {
  private val _loginForm = MutableLiveData<LoginFormState>()
  val loginFormState: LiveData<LoginFormState> = _loginForm
  private val _loginResult = MutableLiveData<LoginResult>()
  val loginResult: LiveData<LoginResult> = _loginResult


  fun login(email: String, password: String) {
    viewModelScope.launch {
      val result = repository.login(email, password)
      _loginResult.value = when (result) {
        is Result.Success -> LoginResult(success = LoggedInUserView(result.data))
        is Result.Error -> LoginResult(error = R.string.login_failed)
      }
    }
  }


  fun loginDataChanged(email: String, password: String) {
    val newState = LoginFormState(
      emailError = if (email.isNotBlank() && !isEmailValid(email)) R.string.invalid_email else null,
      passwordError = if (password.isNotBlank() && !isPasswordValid(password)) R.string.invalid_password else null,
      isDataValid = email.isNotBlank() && password.isNotBlank() && isEmailValid(email) && isPasswordValid(password)
    )
    _loginForm.value = newState
  }


  private fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }


  private fun isPasswordValid(password: String): Boolean {
    return password.length > 5
  }
}