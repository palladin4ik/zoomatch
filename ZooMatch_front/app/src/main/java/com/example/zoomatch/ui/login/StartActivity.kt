package com.example.zoomatch.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.MainActivity
import com.example.zoomatch.R
import com.example.zoomatch.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

  private lateinit var loginViewModel: LoginViewModel
  private lateinit var binding: ActivityStartBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityStartBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val email = binding.userEmail
    val password = binding.password
    val username = binding.username
    val login = binding.login
    val loading = binding.loading

    binding.apply {
      tabLogin?.setOnClickListener { toggleForm(0) }
      tabRegister?.setOnClickListener { toggleForm(1) }
      toggleForm(0)
    }

    loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
      .get(LoginViewModel::class.java)

    loginViewModel.loginFormState.observe(this@StartActivity, Observer {
      val loginState = it ?: return@Observer

      // disable enter button unless both email / password / (username) is valid
      login.isEnabled = loginState.isDataValid

      if (loginState.emailError != null) {
        email.error = getString(loginState.emailError)
      }
      if (loginState.passwordError != null) {
        password.error = getString(loginState.passwordError)
      }
      if (loginState.usernameError != null) {
        username?.error = getString(loginState.usernameError)
      }

    })

    loginViewModel.loginSignupResult.observe(this@StartActivity, Observer {
      val loginResult = it ?: return@Observer

      loading.visibility = View.GONE
      if (loginResult.error != null) {
        showLoginFailed(loginResult.error)
      }
      if (loginResult.success != null) {
        updateUiWithUser(loginResult.success)
      }
      setResult(Activity.RESULT_OK)

      //Complete and destroy login activity once successful
      finish()
    })

    email.afterTextChanged {
      loginViewModel.loginDataChanged(
        email.text.toString(),
        password.text.toString()
      )
    }

    password.apply {
      afterTextChanged {
        loginViewModel.loginDataChanged(
          email.text.toString(),
          password.text.toString()
        )
      }

      setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
          EditorInfo.IME_ACTION_DONE ->
            loginViewModel.login(
              email.text.toString(),
              password.text.toString()
            )
        }
        false
      }

      login.setOnClickListener {
        loading.visibility = View.VISIBLE
        loginViewModel.login(email.text.toString(), password.text.toString())
      }
    }
  }
  private fun toggleForm(param: Int) {
    val tabLogin = binding.tabLogin
    val tabRegister = binding.tabRegister

    tabLogin?.isSelected = param == 0
    tabRegister?.isSelected = param == 1
  }

  private fun updateUiWithUser(model: LoggedInUserView) {
    val welcome = getString(R.string.welcome)
    val displayName = model.displayName
    // TODO : initiate successful logged in experience
    Toast.makeText(
      applicationContext,
      "$welcome $displayName",
      Toast.LENGTH_LONG
    ).show()
    startActivity(Intent(this, MainActivity::class.java))
  }

  private fun showLoginFailed(@StringRes errorString: Int) {
    Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
  }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
  this.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
      afterTextChanged.invoke(editable.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
  })
}