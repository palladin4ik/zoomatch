package com.example.zoomatch.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zoomatch.R
import com.example.zoomatch.databinding.ActivityLayoutWipBinding

class LayoutWip : AppCompatActivity() {

    private lateinit var binding: ActivityLayoutWipBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLayoutWipBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}