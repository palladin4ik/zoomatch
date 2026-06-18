package com.example.zoomatch.ui.startScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.example.zoomatch.R
import com.example.zoomatch.data.UserDisplay
import com.example.zoomatch.databinding.ActivityStartBinding
import com.example.zoomatch.ui.homeScreen.HomeActivity

class StartActivity : AppCompatActivity() {

  private val loginViewModel: LoginViewModel by viewModels {
    LoginViewModelFactory(application)
  }
  private val regViewModel: RegViewModel by viewModels {
    RegViewModelFactory(application)
  }


  private lateinit var binding: ActivityStartBinding
  private var currentMode = 1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityStartBinding.inflate(layoutInflater)
    setContentView(binding.root)

    initListeners()

    loginViewModel.loginFormState.observe(this) { state ->
      val s = state ?: return@observe
      binding.login.isEnabled = s.isDataValid
      binding.email.error = s.emailError?.let { getString(it) }
      binding.password.error = s.passwordError?.let { getString(it) }
    }
    loginViewModel.loginResult.observe(this) { result ->
      val r = result ?: return@observe
      if (r.error != null) showLoginFailed(r.error)
      result.success?.let { model ->
        updateUiWithUser(model)
        finish()
      }
    }

    regViewModel.regFormState.observe(this) { state ->
      val s = state ?: return@observe
      binding.login.isEnabled = s.isDataValid
      binding.email.error = s.emailError?.let { getString(it) }
      binding.password.error = s.passwordError?.let { getString(it) }
      binding.firstnameField.error = s.firstnameError?.let { getString(it) }
      binding.lastnameField.error = s.lastnameError?.let { getString(it) }
    }
    regViewModel.regResult.observe(this) { result ->
      val r = result ?: return@observe
      if (r.error != null) showLoginFailed(r.error)
      result.success?.let { model ->
        updateUiWithUser(model)
        finish()
      }
    }

    val tabLogin = binding.tabLogin
    val tabRegister = binding.tabRegister

    tabLogin.setOnClickListener { toggleForm(0) }
    tabRegister.setOnClickListener { toggleForm(1) }

    toggleForm(0)
    binding.tabGroup.check(R.id.tabLogin)
  }

  private fun initListeners() {
    val email = binding.email
    val password = binding.password
    val firstname = binding.firstnameField
    val lastname = binding.lastnameField
    val login = binding.login

    val handler = Handler(Looper.getMainLooper())
    val debounceDelay = 500L

    fun validateRegistration() {
      val emailText = email.text.toString()
      val passText = password.text.toString()
      val fnText = firstname.text.toString()
      val lnText = lastname.text.toString()
      if (currentMode == 0) loginViewModel.loginDataChanged(emailText, passText)
      else regViewModel.regDataChanged(emailText, passText, fnText, lnText)
    }

    email.afterTextChanged {
      handler.removeCallbacksAndMessages(null)
      handler.postDelayed({ validateRegistration() }, debounceDelay)
    }

    password.apply {
      afterTextChanged {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ validateRegistration() }, debounceDelay)
      }

      setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
          EditorInfo.IME_ACTION_DONE -> {
            if (currentMode == 0) {
              loginViewModel.login(email.text.toString(), password.text.toString())
            }
          }
        }
        false
      }
    }

    firstname.afterTextChanged {
      if (currentMode == 1) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ validateRegistration() }, debounceDelay)
      }
    }

    lastname.afterTextChanged {
      if (currentMode == 1) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ validateRegistration() }, debounceDelay)
      }
    }

    login.setOnClickListener {
      val emailText = email.text.toString()
      val passwordText = password.text.toString()

      if (currentMode == 0) {
        loginViewModel.login(emailText, passwordText)
      } else {
        val fn = firstname.text.toString()
        val ln = lastname.text.toString()
        regViewModel.register(emailText, passwordText, fn, ln)
      }
    }
  }

  private fun toggleForm(param: Int) {
    if (param == currentMode) return
    currentMode = param
    val isRegister = param == 1
    binding.firstnameField.visibility = if (isRegister) View.VISIBLE else View.GONE
    binding.lastnameField.visibility = if (isRegister) View.VISIBLE else View.GONE
    // Keep parent LinearLayout visible/hidden
    binding.firstnameField.parent?.let { (it as View).visibility = if (isRegister) View.VISIBLE else View.GONE }
    binding.login.text =
      if (param == 0) binding.tabLogin.text.toString() else binding.tabRegister.text.toString()
    binding.tabLogin.isSelected = param == 0
    binding.tabRegister.isSelected = param == 1

    binding.email.text?.clear()
    binding.password.text?.clear()
    binding.firstnameField.text?.clear()
    binding.lastnameField.text?.clear()

    binding.email.error = null
    binding.password.error = null
    binding.firstnameField.error = null
    binding.lastnameField.error = null
  }

  private fun updateUiWithUser(model: UserDisplay) {
    val welcome = getString(R.string.welcome)
    val displayName = model.displayName
    Toast.makeText(
      applicationContext, "$welcome $displayName", Toast.LENGTH_LONG
    ).show()
    startActivity(Intent(this, HomeActivity::class.java))
  }

  private fun showLoginFailed(@StringRes errorString: Int) {
    Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
  }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
  this.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
      afterTextChanged.invoke(editable.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
  })
}
