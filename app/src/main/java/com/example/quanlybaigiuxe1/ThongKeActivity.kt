package com.example.quanlybaigiuxe1

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("SetTextI18n")
class ThongKeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    // Khai báo các biến giao diện
    private lateinit var tvSoXeTrongBai: TextView
    private lateinit var tvSoXeMayTrongBai: TextView
    private lateinit var tvSoOtoTrongBai: TextView

    private lateinit var tvTongDoanhThu: TextView
    private lateinit var tvTieuDeDoanhThu: TextView
    private lateinit var tvDoanhThuHomNay: TextView
    private lateinit var tvDoanhThuXeMay: TextView
    private lateinit var tvDoanhThuOto: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thong_ke)

        dbHelper = DatabaseHelper(this)

        // 1. Ánh xạ các thành phần UI
        findViewById<ImageView>(R.id.imgBack).setOnClickListener { finish() }

        tvSoXeTrongBai = findViewById(R.id.tvSoXeTrongBai)
        tvSoXeMayTrongBai = findViewById(R.id.tvSoXeMayTrongBai)
        tvSoOtoTrongBai = findViewById(R.id.tvSoOtoTrongBai)

        tvTongDoanhThu = findViewById(R.id.tvTongDoanhThu)
        tvTieuDeDoanhThu = findViewById(R.id.tvTieuDeDoanhThu)
        tvDoanhThuHomNay = findViewById(R.id.tvDoanhThuHomNay)
        tvDoanhThuXeMay = findViewById(R.id.tvDoanhThuXeMay)
        tvDoanhThuOto = findViewById(R.id.tvDoanhThuOto)

        val btnCalendarFilter = findViewById<CardView>(R.id.btnCalendarFilter)

        // 2. Load dữ liệu cố định (Số xe đang trong bãi và Tổng doanh thu mọi thời đại)
        loadDuLieuCoDinh()

        // 3. Mặc định khi mới vào: Hiển thị doanh thu của ngày hôm nay
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        loadDoanhThuTheoThoiGian(todayStr, "Ngày $todayStr")

        // 4. Bắt sự kiện click nút Lịch: Bỏ tuỳ chọn Tháng, hiện thẳng lịch chọn Ngày
        btnCalendarFilter.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun loadDuLieuCoDinh() {
        val tongXe = dbHelper.getTongXeDangTrongBai()
        val soXeMay = dbHelper.getOccupiedCountByType("Xe máy")
        val soOTo = dbHelper.getOccupiedCountByType("Ô tô")

        tvSoXeTrongBai.text = "$tongXe chiếc"
        tvSoXeMayTrongBai.text = "$soXeMay"
        tvSoOtoTrongBai.text = "$soOTo"

        val tongDoanhThuAll = dbHelper.getTongDoanhThu()
        tvTongDoanhThu.text = "$tongDoanhThuAll VNĐ"
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonth + 1)
            val formattedDay = String.format(Locale.getDefault(), "%02d", selectedDay)

            // Lọc trực tiếp theo NGÀY -> Pattern: "dd-MM-yyyy"
            val pattern = "$formattedDay-$formattedMonth-$selectedYear"
            loadDoanhThuTheoThoiGian(pattern, "Ngày $formattedDay/$formattedMonth/$selectedYear")

        }, year, month, day)

        datePickerDialog.show()
    }

    private fun loadDoanhThuTheoThoiGian(pattern: String, title: String) {
        tvTieuDeDoanhThu.text = title

        val doanhThuTong = dbHelper.getTongDoanhThuTheoThoiGian(pattern)
        val doanhThuXeMay = dbHelper.getDoanhThuLoaiXeTheoThoiGian("Xe máy", pattern)
        val doanhThuOto = dbHelper.getDoanhThuLoaiXeTheoThoiGian("Ô tô", pattern)

        tvDoanhThuHomNay.text = "$doanhThuTong VNĐ"
        tvDoanhThuXeMay.text = "$doanhThuXeMay VNĐ"
        tvDoanhThuOto.text = "$doanhThuOto VNĐ"
    }
}