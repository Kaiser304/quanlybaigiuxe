package com.example.quanlybaigiuxe1

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThongKeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thong_ke)

        // 1. Ánh xạ nút Quay lại
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener {
            finish()
        }

        // 2. Ánh xạ toàn bộ giao diện
        val tvSoXeTrongBai = findViewById<TextView>(R.id.tvSoXeTrongBai)
        val tvSoXeMayTrongBai = findViewById<TextView>(R.id.tvSoXeMayTrongBai) // Mới thêm
        val tvSoOtoTrongBai = findViewById<TextView>(R.id.tvSoOtoTrongBai)   // Mới thêm

        val tvTongDoanhThu = findViewById<TextView>(R.id.tvTongDoanhThu)
        val tvDoanhThuHomNay = findViewById<TextView>(R.id.tvDoanhThuHomNay)
        val tvDoanhThuXeMay = findViewById<TextView>(R.id.tvDoanhThuXeMay)
        val tvDoanhThuOto = findViewById<TextView>(R.id.tvDoanhThuOto)

        // 3. Khởi tạo DatabaseHelper
        val dbHelper = DatabaseHelper(this)

        // --- A. XỬ LÝ NHÓM SỐ LƯỢNG XE ---
        val tongXeTrongBai = dbHelper.getTongXeDangTrongBai()
        tvSoXeTrongBai.text = "Tổng số xe trong bãi: $tongXeTrongBai chiếc"

        val soXeMay = dbHelper.getOccupiedCountByType("Xe máy")
        tvSoXeMayTrongBai.text = "  Xe máy: $soXeMay chiếc"

        val soOTo = dbHelper.getOccupiedCountByType("Ô tô")
        tvSoOtoTrongBai.text = "  Ô tô: $soOTo chiếc"

        // --- B. XỬ LÝ NHÓM DOANH THU ---
        val tongDoanhThu = dbHelper.getTongDoanhThu()
        tvTongDoanhThu.text = "Tổng doanh thu: $tongDoanhThu VNĐ"

        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val ngayHomNay = sdf.format(Date())
        val doanhThuHomNay = dbHelper.getDoanhThuTheoNgay(ngayHomNay)
        tvDoanhThuHomNay.text = "Hôm nay ($ngayHomNay): $doanhThuHomNay VNĐ"

        val doanhThuXeMay = dbHelper.getDoanhThuTheoLoaiXe("Xe máy")
        tvDoanhThuXeMay.text = "Doanh thu Xe máy: $doanhThuXeMay VNĐ"

        val doanhThuOto = dbHelper.getDoanhThuTheoLoaiXe("Ô tô")
        tvDoanhThuOto.text = "Doanh thu Ô tô: $doanhThuOto VNĐ"
    }
}