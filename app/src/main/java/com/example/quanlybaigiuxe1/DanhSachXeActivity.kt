package com.example.quanlybaigiuxe1

import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.TextView
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
        xeAdapter = XeAdapter(danhSachGoc)
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

        // Truy vấn bảng Ticket
        val cursor: Cursor = db.rawQuery("SELECT * FROM Ticket WHERE status = 1", null)

        if (cursor.moveToFirst()) {
            do {
                // Lấy ID cột an toàn bằng getColumnIndex
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val bienSo = cursor.getString(cursor.getColumnIndexOrThrow("plate")) ?: ""
                val loaiXe = cursor.getString(cursor.getColumnIndexOrThrow("type")) ?: "Không rõ"
                val gioVao = cursor.getString(cursor.getColumnIndexOrThrow("time_in")) ?: ""

                // Vì Database chưa có cột ảnh, ta để trống hoặc dùng ảnh mặc định
                val hinhAnh = ""

                danhSachGoc.add(Xe(id, bienSo, loaiXe, gioVao, hinhAnh))
            } while (cursor.moveToNext())
        }
        cursor.close()

        xeAdapter.updateData(danhSachGoc)

        // Hiển thị thông báo nếu bãi trống
        if (danhSachGoc.isEmpty()) {
            tvKhongCoXe.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvKhongCoXe.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}