package com.example.zoomatch.ui

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applySystemBarsPadding() {
  ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    view.setPadding(
      systemBars.left,
      systemBars.top,
      systemBars.right,
      systemBars.bottom
    )
    insets
  }
}

fun View.applySystemBarsMargin() {
  ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    val params = view.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
      params.topMargin = systemBars.top
      view.layoutParams = params
    }
    insets
  }
}
