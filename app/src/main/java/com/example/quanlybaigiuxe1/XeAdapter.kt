package com.example.quanlybaigiuxe1

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Import chuẩn của Glide

class XeAdapter(
    private var danhSachXe: List<Xe>,
    private val onEditClick: (Xe) -> Unit
) : RecyclerView.Adapter<XeAdapter.XeViewHolder>() {

    class XeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBienSo: TextView = itemView.findViewById(R.id.tvBienSo)
        val tvLoaiXe: TextView = itemView.findViewById(R.id.tvLoaiXe)
        val tvGioVao: TextView = itemView.findViewById(R.id.tvGioVao)
        val imgXeChup: ImageView = itemView.findViewById(R.id.imgXeChup)
        val btnEditXe: ImageButton = itemView.findViewById(R.id.btnEditXe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XeViewHolder {
        // Layout item_xe.xml phải có các ID tương ứng ở trên
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_xe, parent, false)
        return XeViewHolder(view)
    }

    override fun onBindViewHolder(holder: XeViewHolder, position: Int) {
        val xe = danhSachXe[position]
        holder.tvBienSo.text = xe.bienSo
        holder.tvLoaiXe.text = xe.loaiXe
        holder.tvGioVao.text = xe.gioVao

        // XỬ LÝ HÌNH ẢNH AN TOÀN: Tránh văng app khi xe.hinhAnh bị null hoặc trống
        if (!xe.hinhAnh.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(xe.hinhAnh)
                .placeholder(R.drawable.ic_car) // Ảnh hiện ra khi đang load
                .error(R.drawable.ic_car)       // Ảnh hiện ra khi đường dẫn lỗi
                .into(holder.imgXeChup)
        } else {
            // Nếu không có ảnh trong DB, hiện icon xe mặc định
            holder.imgXeChup.setImageResource(R.drawable.ic_car)
        }

        // Sự kiện click vào một dòng trong danh sách
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            // LƯU Ý: Phải khai báo ChiTietXeActivity trong Manifest
            val intent = Intent(context, ChiTietXeActivity::class.java)
            intent.putExtra("BIEN_SO", xe.bienSo)
            intent.putExtra("LOAI_XE", xe.loaiXe)
            intent.putExtra("GIO_VAO", xe.gioVao)
            intent.putExtra("HINH_ANH", xe.hinhAnh)
            context.startActivity(intent)
        }

        holder.btnEditXe.setOnClickListener {
            onEditClick(xe)
        }
    }

    override fun getItemCount(): Int = danhSachXe.size

    // Hàm cập nhật danh sách khi tìm kiếm (SearchView)
    fun updateData(newList: List<Xe>) {
        danhSachXe = newList
        notifyDataSetChanged()
    }
}