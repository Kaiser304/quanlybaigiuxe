package com.example.quanlybaigiuxe1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlybaigiuxe1.databinding.ActivityMainBinding
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private var username: String = ""
    private val MAX_XE_MAY = 100
    private val MAX_OTO = 50

    private var currentBannerIndex = 0
    private val banners = intArrayOf(R.drawable.banner1, R.drawable.banner4, R.drawable.banner2, R.drawable.banner5, R.drawable.banner3, R.drawable.banner6)

    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private lateinit var autoScrollRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        username = intent.getStringExtra("USERNAME") ?: "admin"

        if (username == "staff") {
            binding.cardThongKe.visibility = View.GONE
        }

        // --- LOGIC BANNER QUẢNG CÁO TỰ ĐỘNG CÓ HIỆU ỨNG ---
        binding.imgBannerSwitcher.setFactory {
            val imageView = ImageView(applicationContext)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView
        }

        binding.imgBannerSwitcher.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.imgBannerSwitcher.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

        // Đặt tấm đầu tiên
        binding.imgBannerSwitcher.setImageResource(banners[currentBannerIndex])

        val moveToNextBanner = {
            currentBannerIndex = (currentBannerIndex + 1) % banners.size
            binding.imgBannerSwitcher.setImageResource(banners[currentBannerIndex])
        }

        autoScrollRunnable = Runnable {
            moveToNextBanner()
            autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
        }

        // TAO ĐÃ XÓA CÁI ĐỒNG HỒ DƯ THỪA Ở ĐÂY RỒI

        binding.btnBannerLeft.setOnClickListener {
            currentBannerIndex = if (currentBannerIndex - 1 < 0) banners.size - 1 else currentBannerIndex - 1
            binding.imgBannerSwitcher.setImageResource(banners[currentBannerIndex])
            resetAutoScroll()
        }

        binding.btnBannerRight.setOnClickListener {
            moveToNextBanner()
            resetAutoScroll()
        }
        // ---------------------------------------------

        // --- CÁC SỰ KIỆN CLICK ---
        // Trong MainActivity.kt

// 1. Nút Gửi xe (Mở màn hình quét tự động vào)
        // Giả sử đây là nút bấm mở màn hình Quét Vào
        binding.cardXeVao.setOnClickListener {
            val intent = Intent(this, VehicleInActivity::class.java) // Dòng 96
            startActivity(intent)
        }

// 2. Nút Lấy xe (Mở màn hình quét tự động ra)
        binding.cardXeRa.setOnClickListener {
            startActivity(Intent(this, LayxeActivity::class.java))
        }

// 3. XÓA BỎ hoàn toàn các đoạn code liên quan đến showInputPlateDialog
// (vì bạn đã dùng quét tự động rồi mà!)

        binding.cardDanhSach.setOnClickListener {
            val intent = Intent(this, DanhSachXeActivity::class.java)
            startActivity(intent)
        }

        binding.cardThongKe.setOnClickListener {
            val intent = Intent(this, ThongKeActivity::class.java)
            startActivity(intent)
        }
        val btnMenuHeader = findViewById<ImageButton>(R.id.btnMenuHeader)

        btnMenuHeader.setOnClickListener { view: View ->
            val popup = PopupMenu(this, view)
            // Thêm mục "Đăng xuất" vào menu
            popup.menu.add("Đăng xuất")

            // Xử lý khi người dùng chọn "Đăng xuất"
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Đăng xuất") {
                    performLogout()
                }
                true
            }
            popup.show()
        }

        //lấy tên user đăng nhập để hiển thị
        if (username.isNotEmpty() ) {
            val nameFromDb = dbHelper.getUserName(username) // Truy vấn bằng 'username'
            binding.tvUserName.text = nameFromDb
        }

    }
    //HÀM ĐĂNG XUẤT
    private fun performLogout() {
        // Thông báo cho người dùng
        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show()

        // Chuyển hướng về màn hình Đăng nhập (LoginActivity)
        val intent = Intent(this, LoginActivity::class.java)
        // Xóa sạch lịch sử các màn hình trước đó để người dùng không bấm "Back" quay lại được
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun resetAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onPause() {
        super.onPause()
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onResume() {
        super.onResume()
        updateAvailableSlots()
        // Chỉ dùng duy nhất 1 đồng hồ chạy lúc app mở lên màn hình
        resetAutoScroll()
    }

    private fun updateAvailableSlots() {
        val occupiedXeMay = dbHelper.getOccupiedCountByType("Xe máy")
        val occupiedOto = dbHelper.getOccupiedCountByType("Ô tô")

        val availableXeMay = MAX_XE_MAY - occupiedXeMay
        val availableOto = MAX_OTO - occupiedOto

        binding.tvXeMayStatus.text = ": ${if(availableXeMay < 0) 0 else availableXeMay} / $MAX_XE_MAY"
        binding.tvOtoStatus.text = ": ${if(availableOto < 0) 0 else availableOto} / $MAX_OTO"

        binding.tvXeMayStatus.setTextColor(if (availableXeMay < 5) android.graphics.Color.RED else android.graphics.Color.parseColor("#4898EF"))
        binding.tvOtoStatus.setTextColor(if (availableOto < 5) android.graphics.Color.RED else android.graphics.Color.parseColor("#FFEB3B"))
    }
     //HIỆN RA BẢNG NHẬP BỂN SỐ XE CẦN LẤY
   /* private fun showInputPlateDialog() {
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
*/
//    private fun openFeature(title: String) {
//        val intent = Intent(this, FeatureActivity::class.java)
//        intent.putExtra("title", title)
//        startActivity(intent)
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
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