package com.example.quanlybaigiuxe1


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.quanlybaigiuxe1.databinding.ActivityVehicleInBinding // Đảm bảo đã bật viewBinding trong build.gradle
import java.text.SimpleDateFormat
import java.util.*


class VehicleInActivity : AppCompatActivity() {


    private lateinit var binding: ActivityVehicleInBinding
    private lateinit var db: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Đồng bộ tắt Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        // Sử dụng ViewBinding cho đồng bộ với MainActivity
        binding = ActivityVehicleInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        db = DatabaseHelper(this)


        // Hiển thị thời gian tạm thời lúc mở màn hình
        updateTimeDisplay()
        binding.btnBack.setOnClickListener {
            finish() // Đóng màn hình hiện tại để quay về MainActivity
        }
        binding.btnLuu.setOnClickListener {
            val bienSo = binding.edtBienSo.text.toString().trim()
            val loaiXe = binding.edtLoaiXe.text.toString().trim()
            //val cardId = binding.edtCardId.text.toString().trim()


            if (bienSo.isEmpty() || loaiXe.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Lấy thời gian chính xác tại ĐÚNG thời điểm bấm nút Lưu
            val realTimeIn = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())


            val result = db.insertVehicle(bienSo, loaiXe, realTimeIn,"IN")


            if (result != -1L) {
                Toast.makeText(this, "Gửi xe thành công!", Toast.LENGTH_SHORT).show()
                finish() // Đóng màn hình này để quay về MainActivity
            } else {
                Toast.makeText(this, "Lỗi! Không thể lưu dữ liệu", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun updateTimeDisplay() {
        val currentTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        binding.txtTimeIn.text = "Thời gian vào: $currentTime"
    }


    private fun clearForm() {
        binding.edtBienSo.setText("")
        binding.edtLoaiXe.setText("")
        //binding.edtCardId.setText("")
    }
}
