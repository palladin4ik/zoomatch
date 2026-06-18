package com.example.zoomatch.ui.homeScreen

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.homeScreen.pets.PetsDataSource
import com.example.zoomatch.data.homeScreen.pets.PetsRepository
import com.example.zoomatch.data.homeScreen.profile.ProfileDataSource
import com.example.zoomatch.data.homeScreen.profile.ProfileRepository
import com.example.zoomatch.data.homeScreen.search.MatchDataSource
import com.example.zoomatch.data.homeScreen.search.MatchRepository
import com.example.zoomatch.data.homeScreen.search.RequestDataSource
import com.example.zoomatch.data.homeScreen.search.RequestRepository
import com.example.zoomatch.data.homeScreen.settings.SettingsDataSource
import com.example.zoomatch.data.homeScreen.settings.SettingsRepository
import com.example.zoomatch.ui.homeScreen.pets.EditPetViewModel
import com.example.zoomatch.ui.homeScreen.pets.PetsViewModel
import com.example.zoomatch.ui.homeScreen.profile.EditProfileViewModel
import com.example.zoomatch.ui.homeScreen.profile.ProfileViewModel
import com.example.zoomatch.ui.homeScreen.search.Fragments.RequestViewModel
import com.example.zoomatch.ui.homeScreen.search.Fragments.SearchViewModel
import com.example.zoomatch.ui.homeScreen.search.MatchingViewModel
import com.example.zoomatch.ui.homeScreen.settings.SettingsViewModel
import com.example.zoomatch.ui.homeScreen.settings.utils.EditPassViewModel

class HomeViewModelFactory(app: Application) : ViewModelProvider.Factory {
  private val tokenManager = TokenManager(app)
  private val db = AppDatabase.getDatabase(app)

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when {
      modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
        val repo = ProfileRepository(ProfileDataSource(), tokenManager, db.userDao())
        ProfileViewModel(repo, db.userDao()) as T
      }

      modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
        val repo = SettingsRepository(SettingsDataSource(), tokenManager, db.userDao())
        SettingsViewModel(repo) as T
      }

      modelClass.isAssignableFrom(EditProfileViewModel::class.java) -> {
        val repo = ProfileRepository(ProfileDataSource(), tokenManager, db.userDao())
        EditProfileViewModel(repo) as T
      }

      modelClass.isAssignableFrom(EditPassViewModel::class.java) -> {
        val repo = SettingsRepository(SettingsDataSource(), tokenManager, db.userDao())
        EditPassViewModel(repo) as T
      }

      modelClass.isAssignableFrom(PetsViewModel::class.java) -> {
        val repo = PetsRepository(
          PetsDataSource(),
          tokenManager,
          db.petDao(),
          db.animalTypeDao(),
          db.breedDao(),
          db.userDao()
        )
        PetsViewModel(repo) as T
      }

      modelClass.isAssignableFrom(EditPetViewModel::class.java) -> {
        val repo = PetsRepository(
          PetsDataSource(),
          tokenManager,
          db.petDao(),
          db.animalTypeDao(),
          db.breedDao(),
          db.userDao()
        )
        EditPetViewModel(repo, db.animalTypeDao(), db.breedDao()) as T
      }

      modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
        val repo = PetsRepository(
          PetsDataSource(), tokenManager,
          db.petDao(), db.animalTypeDao(), db.breedDao(), db.userDao()
        )
        SearchViewModel(repo) as T
      }

      modelClass.isAssignableFrom(MatchingViewModel::class.java) -> {
        val repo = MatchRepository(MatchDataSource(), tokenManager)
        MatchingViewModel(repo) as T
      }

      modelClass.isAssignableFrom(RequestViewModel::class.java) -> {
        val repo = RequestRepository(RequestDataSource(), tokenManager)
        RequestViewModel(repo) as T
      }

      else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
