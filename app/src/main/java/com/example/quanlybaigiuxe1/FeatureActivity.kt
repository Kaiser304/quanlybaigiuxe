package com.example.quanlybaigiuxe1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlybaigiuxe1.databinding.ActivityFeatureBinding

class FeatureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Title
        binding.toolbar.title =
            intent.getStringExtra("title") ?: "Chức năng"

        // Back button
        binding.toolbar.setNavigationOnClickListener {
            finish() // quay lại MainActivity
        }
    }
}
