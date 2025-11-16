package com.example.zoomatch.data.startScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zoomatch.data.db.Network
import com.example.zoomatch.data.db.RefreshRequest
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.ui.homeScreen.HomeActivity
import com.example.zoomatch.ui.startScreen.StartActivity
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

  private val tokenManager by lazy { TokenManager(this) }
  private val api by lazy { Network.zooMatchApi }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      val access = tokenManager.getAccessToken()

      if (access != null) {
        try {
          // TODO: обоновить данные пользователя
          val response = api.getProfile("Bearer $access")
          if (response.isSuccessful) {
            goToHome()
            return@launch
          }
        } catch (e: Exception) { /* игнор */
        }

        // токен умер → refresh
        val refreshed = refreshToken()
        if (refreshed) goToHome() else goToLogin()
      } else {
        goToLogin()
      }
    }
  }

  private suspend fun refreshToken(): Boolean {
    val refresh = tokenManager.getRefreshToken() ?: return false
    return try {
      val response = api.refreshToken(RefreshRequest(refresh))
      if (response.isSuccessful && response.body() != null) {
        val newAccess = response.body()!!.access
        tokenManager.saveTokens(newAccess, refresh)
        true
      } else false
    } catch (e: Exception) {
      tokenManager.clearTokens()
      false
    }
  }

  private fun goToHome() {
    startActivity(Intent(this, HomeActivity::class.java))
    finish()
  }

  private fun goToLogin() {
    startActivity(Intent(this, StartActivity::class.java))
    finish()
  }
}