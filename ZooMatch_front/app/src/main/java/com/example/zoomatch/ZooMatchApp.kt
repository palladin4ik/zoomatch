package com.example.zoomatch

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZooMatchApp : Application() {
  override fun onCreate() {
    super.onCreate()
    MapKitFactory.setApiKey("ad2824c2-51b6-4e70-a2ec-723514800de5")

    val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
    val themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    AppCompatDelegate.setDefaultNightMode(themeMode)
  }
}
