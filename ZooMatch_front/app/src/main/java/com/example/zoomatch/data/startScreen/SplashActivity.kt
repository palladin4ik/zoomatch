package com.example.zoomatch.data.startScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.AnimalTypeEntity
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.BreedEntity
import com.example.zoomatch.data.db.Network
import com.example.zoomatch.data.db.RefreshRequest
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.ui.homeScreen.HomeActivity
import com.example.zoomatch.ui.startScreen.StartActivity
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

  private val tokenManager by lazy { TokenManager(this) }
  private val dataSource by lazy { LoginDataSource() }
  private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      val access = tokenManager.getAccessToken()

      if (access != null) {
        val updated = updateUserData("Bearer $access")
        if (updated) {
          goToHome()
          return@launch
        }

        // токен умер → refresh
        val refreshed = refreshToken()
        if (refreshed && updateUserData("Bearer ${tokenManager.getAccessToken()!!}")) {
          goToHome()
        } else {
          goToLogin()
        }
      } else {
        goToLogin()
      }
    }
  }

  private suspend fun updateUserData(auth: String): Boolean {
    return try {
      val userResult = dataSource.getUserInfo(auth.substring(7))
      if (userResult !is Result.Success) return false
      val (user, pets) = userResult.data

      val typesResult = dataSource.getAnimalTypes(auth.substring(7))
      if (typesResult !is Result.Success) return false

      val breedsResult = dataSource.getBreeds(auth.substring(7))
      if (breedsResult !is Result.Success) return false

      val existingPetsMap = userDao.getAllPets().associateBy { it.id }

      val mergedPets = pets.map { serverPet ->
        existingPetsMap[serverPet.id]?.copy(
          name = serverPet.name,
          is_male = serverPet.is_male,
          age = serverPet.age,
          avatar = serverPet.avatar ?: existingPetsMap[serverPet.id]?.avatar,
          is_active = serverPet.is_active,
          location = serverPet.location ?: existingPetsMap[serverPet.id]?.location,
          description = serverPet.description ?: existingPetsMap[serverPet.id]?.description
        ) ?: serverPet
      }

      val types = typesResult.data.map { AnimalTypeEntity(it.id, it.name) }
      val breeds = breedsResult.data.map { BreedEntity(it.id, it.name, it.animal_type.id) }

      userDao.insertAllAnimalTypes(types)
      userDao.insertAllBreeds(breeds)
      userDao.insert(user)
      userDao.insertAllPets(mergedPets)

      true
    } catch (e: Exception) {
      false
    }
  }

  private suspend fun refreshToken(): Boolean {
    val refresh = tokenManager.getRefreshToken() ?: return false
    return try {
      val response = Network.zooMatchApi.refreshToken(RefreshRequest(refresh))
      if (response.isSuccessful && response.body() != null) {
        tokenManager.saveTokens(response.body()!!.access, refresh)
        true
      } else {
        tokenManager.clearTokens()
        false
      }
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