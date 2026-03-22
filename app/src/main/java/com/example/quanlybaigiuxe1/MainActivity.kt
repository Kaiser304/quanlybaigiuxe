package com.example.quanlybaigiuxe1

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlybaigiuxe1.databinding.ActivityMainBinding
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private var username: String = ""
    private val MAX_XE_MAY = 100
    private val MAX_OTO = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Lấy tên đăng nhập từ Intent
        username = intent.getStringExtra("USERNAME") ?: "admin"

        // Phân quyền: Nếu là nhân viên (staff) thì ẩn nút Thống kê
        if (username == "staff") {
            binding.cardThongKe.visibility = View.GONE
        }

        // --- CÁC SỰ KIỆN CLICK ---

        binding.cardXeVao.setOnClickListener {
            // Mở màn hình Gửi xe chuyên dụng bạn mới copy vào
            val intent = Intent(this, VehicleInActivity::class.java)
            startActivity(intent)
        }

        binding.cardXeRa.setOnClickListener {
            showInputPlateDialog()
        }

        binding.cardDanhSach.setOnClickListener {
            val intent = Intent(this, DanhSachXeActivity::class.java)
            startActivity(intent)
        }

        binding.cardThongKe.setOnClickListener {
            openFeature("THỐNG KÊ")
        }

        // ĐOẠN CODE LƯU XE MẪU CŨ ĐÃ ĐƯỢC XÓA TẠI ĐÂY
    }

    // Hàm cập nhật số chỗ trống dựa trên dữ liệu thật trong DB
    private fun updateAvailableSlots() {
        val occupiedXeMay = dbHelper.getOccupiedCountByType("Xe máy")
        val occupiedOto = dbHelper.getOccupiedCountByType("Ô tô")

        val availableXeMay = MAX_XE_MAY - occupiedXeMay
        val availableOto = MAX_OTO - occupiedOto

        // Hiển thị lên Header
        binding.tvXeMayStatus.text = "Xe máy trống: ${if(availableXeMay < 0) 0 else availableXeMay} / $MAX_XE_MAY"
        binding.tvOtoStatus.text = "Ô tô trống: ${if(availableOto < 0) 0 else availableOto} / $MAX_OTO"

        // Đổi màu cảnh báo nếu sắp hết chỗ
        binding.tvXeMayStatus.setTextColor(if (availableXeMay < 5) android.graphics.Color.RED else android.graphics.Color.WHITE)
        binding.tvOtoStatus.setTextColor(if (availableOto < 5) android.graphics.Color.RED else android.graphics.Color.parseColor("#FFEB3B"))
    }

    override fun onResume() {
        super.onResume()
        // Mỗi khi từ màn hình Gửi xe/Lấy xe quay về, số chỗ trống sẽ tự cập nhật
        updateAvailableSlots()
    }

    private fun showInputPlateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nhập biển số xe cần lấy")

        val input = EditText(this)
        input.hint = "Ví dụ: 67K1-123.45"
        builder.setView(input)

        builder.setPositiveButton("Xác nhận") { _, _ ->
            val bienSo = input.text.toString().trim()
            if (bienSo.isNotEmpty()) {
                val intent = Intent(this, LayxeActivity::class.java)
                intent.putExtra("BIEN_SO", bienSo)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Vui lòng nhập biển số!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun openFeature(title: String) {
        val intent = Intent(this, FeatureActivity::class.java)
        intent.putExtra("title", title)
        startActivity(intent)
    }

    // --- CÀI ĐẶT MENU ---

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Hiển thị tên người dùng đang đăng nhập lên Menu
        val menuItem = menu.findItem(R.id.menu_user)
        menuItem?.actionView?.let {
            it.findViewById<TextView>(R.id.tvUsername)?.text = username
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}