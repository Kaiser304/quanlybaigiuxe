package com.example.quanlybaigiuxe1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlybaigiuxe1.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.edtFullName.text.toString()
            val username = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirm = binding.edtConfirmPassword.text.toString()
            val db = DatabaseHelper(this)
            val database = db.writableDatabase

            if (fullName.isEmpty() || username.isEmpty() ||
                password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // CHECK TRÙNG USERNAME
            val checkCursor = database.rawQuery(
                "SELECT * FROM User WHERE username=?",
                arrayOf(username)
            )

            if (checkCursor.moveToFirst()) {
                Toast.makeText(this, "Username đã tồn tại", Toast.LENGTH_SHORT).show()
                checkCursor.close()
                return@setOnClickListener
            }
            checkCursor.close()

            // LƯU VÀO DATABASE
            val sql = "INSERT INTO User(username, password) VALUES(?, ?)"
            val statement = database.compileStatement(sql)
            statement.bindString(1, username)
            statement.bindString(2, password)
            statement.executeInsert()

            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()

            // Quay về Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.tvBackLogin.setOnClickListener {
            finish() // quay lại Login
        }
    }
}
