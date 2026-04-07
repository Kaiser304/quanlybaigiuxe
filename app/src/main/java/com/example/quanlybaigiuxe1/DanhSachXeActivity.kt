package com.example.quanlybaigiuxe1

import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
            showEditDialog(xeCanSua)
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

                val hinhAnh = ""

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

    private fun showEditDialog(xe: Xe) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sửa thông tin xe")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputBienSo = EditText(this)
        inputBienSo.setText(xe.bienSo)
        layout.addView(inputBienSo)

        val inputLoaiXe = EditText(this)
        inputLoaiXe.setText(xe.loaiXe)
        layout.addView(inputLoaiXe)

        builder.setView(layout)

        builder.setPositiveButton("Lưu") { _, _ ->
            val bienSoMoi = inputBienSo.text.toString().trim()
            val loaiXeMoi = inputLoaiXe.text.toString().trim()

            if (bienSoMoi.isNotEmpty() && loaiXeMoi.isNotEmpty()) {
                val db = dbHelper.writableDatabase
                db.execSQL("UPDATE Ticket SET plate = ?, type = ? WHERE id = ?", arrayOf(bienSoMoi, loaiXeMoi, xe.id))
                Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                loadDanhSachXe()
            } else {
                Toast.makeText(this, "Không được để trống!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}