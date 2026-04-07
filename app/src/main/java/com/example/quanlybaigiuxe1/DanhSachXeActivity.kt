package com.example.quanlybaigiuxe1

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class DanhSachXeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tvKhongCoXe: TextView
    private lateinit var xeAdapter: XeAdapter
    private var danhSachGoc = ArrayList<Xe>()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_danh_sach_xe)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDanhSachXe)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewXe)
        searchView = findViewById(R.id.searchViewXe)
        tvKhongCoXe = findViewById(R.id.tvKhongCoXe)
        dbHelper = DatabaseHelper(this)

        recyclerView.layoutManager = LinearLayoutManager(this)

        xeAdapter = XeAdapter(danhSachGoc) { xeCanSua ->
            showCustomEditDialog(xeCanSua)
        }

        recyclerView.adapter = xeAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filterList = danhSachGoc.filter { xe ->
                    xe.bienSo.contains(newText ?: "", ignoreCase = true)
                }
                xeAdapter.updateData(filterList)

                if (filterList.isEmpty() && danhSachGoc.isNotEmpty()) {
                    tvKhongCoXe.visibility = View.VISIBLE
                    tvKhongCoXe.text = "Không tìm thấy xe"
                    recyclerView.visibility = View.GONE
                } else if (danhSachGoc.isNotEmpty()) {
                    tvKhongCoXe.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadDanhSachXe()
    }

    private fun loadDanhSachXe() {
        danhSachGoc.clear()
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery("SELECT * FROM Ticket WHERE status = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val bienSo = cursor.getString(cursor.getColumnIndexOrThrow("plate")) ?: ""
                val loaiXe = cursor.getString(cursor.getColumnIndexOrThrow("type")) ?: "Không rõ"
                val gioVao = cursor.getString(cursor.getColumnIndexOrThrow("time_in")) ?: ""

                // Lấy đường dẫn ảnh nếu nhóm mày có lưu
                val hinhAnh = try {
                    cursor.getString(cursor.getColumnIndexOrThrow("image_path")) ?: ""
                } catch (e: Exception) {
                    ""
                }

                danhSachGoc.add(Xe(id, bienSo, loaiXe, gioVao, hinhAnh))
            } while (cursor.moveToNext())
        }
        cursor.close()

        xeAdapter.updateData(danhSachGoc)

        if (danhSachGoc.isEmpty()) {
            tvKhongCoXe.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvKhongCoXe.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // Hàm gọi cái bảng Custom màu cam lên
    private fun showCustomEditDialog(xe: Xe) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_xe_custom, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        // Dòng này làm cho cái viền vuông màu trắng mờ đi, chỉ chừa lại cái bảng bo tròn của mày
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val inputBienSo = dialogView.findViewById<EditText>(R.id.inputBienSoCustom)
        val rbXeMay = dialogView.findViewById<RadioButton>(R.id.rbXeMayCustom)
        val rbOto = dialogView.findViewById<RadioButton>(R.id.rbOtoCustom)
        val btnConfirmEdit = dialogView.findViewById<AppCompatButton>(R.id.btnConfirmEditCustom)

        // Hiện thông tin cũ lên bảng
        inputBienSo.setText(xe.bienSo)
        if (xe.loaiXe.equals("Ô tô", ignoreCase = true)) {
            rbOto.isChecked = true
        } else {
            rbXeMay.isChecked = true
        }

        // Bấm xác nhận sửa
        btnConfirmEdit.setOnClickListener {
            val bienSoMoi = inputBienSo.text.toString().trim()
            val loaiXeMoi = if (rbOto.isChecked) "Ô tô" else "Xe máy"

            if (bienSoMoi.isNotEmpty()) {
                val db = dbHelper.writableDatabase
                db.execSQL("UPDATE Ticket SET plate = ?, type = ? WHERE id = ?", arrayOf(bienSoMoi, loaiXeMoi, xe.id))
                Toast.makeText(this, "Đã cập nhật thành công!", Toast.LENGTH_SHORT).show()
                loadDanhSachXe() // Load lại danh sách
                dialog.dismiss() // Tắt cái bảng đi
            } else {
                Toast.makeText(this, "Không được để trống biển số!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}