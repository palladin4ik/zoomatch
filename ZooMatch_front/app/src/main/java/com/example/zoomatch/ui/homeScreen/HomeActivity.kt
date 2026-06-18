package com.example.zoomatch.ui.homeScreen

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.zoomatch.R
import com.example.zoomatch.databinding.ActivityHomeBinding
import com.example.zoomatch.ui.applySystemBarsMargin
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

  private lateinit var binding: ActivityHomeBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.container.applySystemBarsMargin()

    val navView: BottomNavigationView = binding.navView

    val navHostFragment = supportFragmentManager
      .findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment

    val navController = navHostFragment.navController
    navView.setupWithNavController(navController)
  }
}
