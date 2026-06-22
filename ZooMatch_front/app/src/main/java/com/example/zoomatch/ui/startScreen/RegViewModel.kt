package com.example.zoomatch.ui.startScreen

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.R
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.startScreen.RegDataSource
import com.example.zoomatch.data.startScreen.RegFormState
import com.example.zoomatch.data.startScreen.RegRepository
import com.example.zoomatch.data.startScreen.RegResult
import com.example.zoomatch.data.startScreen.RegUserView
import kotlinx.coroutines.launch

class RegViewModel(private val repository: RegRepository) : ViewModel() {
  private val _regForm = MutableLiveData<RegFormState>()
  val regFormState: LiveData<RegFormState> = _regForm
  private val _regResult = MutableLiveData<RegResult>()
  val regResult: LiveData<RegResult> = _regResult


  fun register(email: String, password: String, firstname: String, lastname: String) {
    viewModelScope.launch {
      val result = repository.registerAndLogin(firstname, lastname, email, password)
      _regResult.value = when (result) {
        is Result.Success -> RegResult(success = RegUserView(result.data))
        is Result.Error -> {
          val errorRes = if (result.message == RegDataSource.EMAIL_EXISTS) {
            R.string.email_already_exists
          } else {
            R.string.login_failed
          }
          RegResult(error = errorRes)
        }
      }
    }
  }


  fun regDataChanged(email: String, password: String, firstname: String, lastname: String) {
    val newState = RegFormState(
      emailError = if (email.isNotBlank() && !isEmailValid(email)) R.string.invalid_email else null,
      passwordError = if (password.isNotBlank() && !isPasswordValid(password)) R.string.invalid_password else null,
      firstnameError = if (firstname.isNotBlank() && firstname.isBlank()) R.string.invalid_name else null,
      lastnameError = if (lastname.isNotBlank() && lastname.isBlank()) R.string.invalid_name else null,
      isDataValid = email.isNotBlank() && password.isNotBlank() && firstname.isNotBlank() && lastname.isNotBlank()
          && isEmailValid(email) && isPasswordValid(password)
    )
    _regForm.value = newState
  }


  private fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }


  private fun isPasswordValid(password: String): Boolean {
    return password.length > 5
  }
}
