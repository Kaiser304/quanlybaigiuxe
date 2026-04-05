package com.example.quanlybaigiuxe1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlybaigiuxe1.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý nút Đăng nhập
        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = DatabaseHelper(this)
            val database = db.readableDatabase

            val cursor = database.rawQuery(
                "SELECT * FROM User WHERE username=? AND password=?",
                arrayOf(username, password)
            )
            if (cursor.moveToFirst()) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                // chuyển sang màn hình chính
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
            }
            cursor.close()

//            // Login giả (chưa DB)
//            if ((username == "admin" && password == "123") || (username == "staff" && password == "456")) {
//                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
//
//                // Sang màn hình Main
//                val intent = Intent(this, MainActivity::class.java)
//                intent.putExtra("USERNAME", username) // USERNAME = admin hoặc staff
//                startActivity(intent)
//                finish() // không quay lại login khi bấm back
//            } else {
//                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
//            }

        }

        // Sang màn hình đăng ký
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
