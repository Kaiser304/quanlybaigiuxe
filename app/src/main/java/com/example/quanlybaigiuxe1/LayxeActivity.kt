package com.example.quanlybaigiuxe1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlybaigiuxe1.databinding.ActivityLayxeBinding
import java.text.SimpleDateFormat
import java.util.*

class LayxeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLayxeBinding
    private lateinit var dbHelper: DatabaseHelper

    // Đã có sẵn 2 biến này để lưu kết quả tính toán
    private var finalPrice: Int = 0
    private var finalTimeOut: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayxeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        val bienSo = intent.getStringExtra("BIEN_SO") ?: ""
        loadThongTinXe(bienSo)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnThanhToan.setOnClickListener {
            thanhToanXeRa(bienSo)
        }
    }

    private fun loadThongTinXe(bienSo: String) {
        val ticket = dbHelper.getTicketByPlate(bienSo)
        if (ticket != null) {
            binding.tvBienSo.text = ticket.plate
            binding.tvGioVao.text = ticket.time_in

            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val dateRa = Date()

            // SỬA TẠI ĐÂY: Lưu giờ ra vào biến toàn cục để tí nữa lưu DB
            finalTimeOut = sdf.format(dateRa)
            binding.tvGioRa.text = finalTimeOut

            try {
                val dateVao = sdf.parse(ticket.time_in)!!
                val diff = dateRa.time - dateVao.time

                val diffMinutes = diff / (1000 * 60)
                val diffHours = diffMinutes / 60

                binding.tvTongThoiGian.text = "${diffHours}h ${diffMinutes % 60}m"

                // Tính tiền
                val hoursToCharge = if (diffMinutes < 60) 1
                else (diffMinutes / 60.0).let { Math.ceil(it).toLong() }

                // SỬA TẠI ĐÂY: Lưu giá tiền vào biến toàn cục
                finalPrice = (hoursToCharge * 5000).toInt()

                binding.tvGiaTien.text = String.format("%,d", finalPrice)
            } catch (e: Exception) {
                binding.tvTongThoiGian.text = "Lỗi tính toán"
            }
        } else {
            Toast.makeText(this, "Không tìm thấy xe trong bãi!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun thanhToanXeRa(bienSo: String) {
        // SỬA TẠI ĐÂY: Truyền thêm giờ ra và giá tiền vào hàm update
        val success = dbHelper.updateStatusXeRa(bienSo, finalTimeOut, finalPrice)
        if (success) {
            Toast.makeText(this, "Thanh toán thành công: ${finalPrice} VNĐ", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Lỗi cập nhật Database!", Toast.LENGTH_SHORT).show()
        }
    }
}