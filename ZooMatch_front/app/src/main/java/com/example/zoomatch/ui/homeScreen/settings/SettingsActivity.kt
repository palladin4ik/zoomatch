package com.example.zoomatch.ui.homeScreen.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.data.Result
import com.example.zoomatch.databinding.HomeFragmentSettingsBinding
import com.example.zoomatch.ui.LayoutWip
import com.example.zoomatch.ui.applySystemBarsPadding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.profile.EditProfileActivity
import com.example.zoomatch.ui.homeScreen.settings.utils.EditPassActivity
import com.example.zoomatch.ui.startScreen.StartActivity
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

  private lateinit var binding: HomeFragmentSettingsBinding

  private val viewModel: SettingsViewModel by viewModels {
    HomeViewModelFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = HomeFragmentSettingsBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.root.applySystemBarsPadding()

    setupUI()
    observeData()
  }

  private fun setupUI() {
    binding.backArrow.setOnClickListener { finish() }

    binding.switchPush.setOnClickListener {
      Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show()
    }

    binding.editProfileLayout.setOnClickListener {
      startActivity(Intent(this, EditProfileActivity::class.java))
    }

    binding.logOutLayout.setOnClickListener { viewModel.logoutClick() }
    binding.privacyPolicyLayout.setOnClickListener { openWip() }
    binding.termsOfUseLayout.setOnClickListener { openWip() }
    binding.editPasswordLayout.setOnClickListener { editPass() }
    binding.deleteAccountLayout.setOnClickListener { confirmDelete() }

    setupThemeSelector()
  }

  private fun setupThemeSelector() {
    val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val currentMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    updateThemeLabel(currentMode)

    binding.themeLayout.setOnClickListener {
      val modes = intArrayOf(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES
      )
      val labels = arrayOf("Системная", "Светлая", "Тёмная")
      val checkedIndex = modes.indexOf(currentMode).coerceAtLeast(0)

      AlertDialog.Builder(this)
        .setTitle("Тема оформления")
        .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
          val selectedMode = modes[which]
          prefs.edit().putInt("theme_mode", selectedMode).apply()
          AppCompatDelegate.setDefaultNightMode(selectedMode)
          updateThemeLabel(selectedMode)
          dialog.dismiss()
        }
        .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
        .show()
    }
  }

  private fun updateThemeLabel(mode: Int) {
    val label = when (mode) {
      AppCompatDelegate.MODE_NIGHT_NO -> "Светлая"
      AppCompatDelegate.MODE_NIGHT_YES -> "Тёмная"
      else -> "Системная"
    }
    binding.themeValue.text = label
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.logout.collect {
            finish()
            startActivity(Intent(this@SettingsActivity, StartActivity::class.java))
          }
        }
        launch {
          viewModel.delete.collect { result ->
            when (result) {
              is Result.Success -> {
                Toast.makeText(this@SettingsActivity, result.data, Toast.LENGTH_SHORT).show()
                finish()
                startActivity(Intent(this@SettingsActivity, StartActivity::class.java))
              }
              is Result.Error -> {
                Toast.makeText(this@SettingsActivity, result.message, Toast.LENGTH_LONG).show()
                Log.e("error", result.message)
              }
            }
          }
        }
      }
    }
  }

  private fun openWip() {
    startActivity(Intent(this, LayoutWip::class.java))
  }

  private fun editPass() {
    startActivity(Intent(this, EditPassActivity::class.java))
  }

  private fun confirmDelete() {
    AlertDialog.Builder(this)
      .setTitle("Удаление аккаунта")
      .setMessage("Вы уверены?")
      .setPositiveButton("Да") { _, _ -> viewModel.deleteClick() }
      .setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
      .show()
  }
}
