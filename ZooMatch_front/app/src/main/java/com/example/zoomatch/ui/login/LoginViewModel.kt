package com.example.zoomatch.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zoomatch.R
import com.example.zoomatch.data.LoginRepository
import com.example.zoomatch.data.Result

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

  private val _loginForm = MutableLiveData<LoginFormState>()
  val loginFormState: LiveData<LoginFormState> = _loginForm

  private val _loginSignupResult = MutableLiveData<LoginSignupResult>()
  val loginSignupResult: LiveData<LoginSignupResult> = _loginSignupResult

  fun login(username: String, password: String) {
    // can be launched in a separate asynchronous job
    val result = loginRepository.login(username, password)

    if (result is Result.Success) {
      _loginSignupResult.value =
        LoginSignupResult(success = LoggedInUserView(displayName = result.data.displayName))
    } else {
      _loginSignupResult.value = LoginSignupResult(error = R.string.login_failed)
    }
  }

  fun loginDataChanged(username: String, password: String) {
    if (!isUserNameValid(username)) {
      _loginForm.value = LoginFormState(emailError = R.string.invalid_username)
    } else if (!isPasswordValid(password)) {
      _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
    } else {
      _loginForm.value = LoginFormState(isDataValid = true)
    }
  }

  // A placeholder username validation check
  private fun isUserNameValid(username: String): Boolean {
    return if (username.contains('@')) {
      Patterns.EMAIL_ADDRESS.matcher(username).matches()
    } else {
      username.isNotBlank()
    }
  }

  // A placeholder password validation check
  private fun isPasswordValid(password: String): Boolean {
    return password.length > 5
  }
}