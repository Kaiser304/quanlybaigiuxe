package com.example.quanlybaigiuxe1

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ChiTietXeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chi_tiet_xe)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarChiTiet)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val tvBienSo = findViewById<TextView>(R.id.tvDetailBienSo)
        val tvLoaiXe = findViewById<TextView>(R.id.tvDetailLoaiXe)
        val tvGioVao = findViewById<TextView>(R.id.tvDetailGioVao)

        tvBienSo.text = intent.getStringExtra("BIEN_SO")
        tvLoaiXe.text = "Loại xe: ${intent.getStringExtra("LOAI_XE")}"
        tvGioVao.text = "Giờ vào: ${intent.getStringExtra("GIO_VAO")}"
    }
}