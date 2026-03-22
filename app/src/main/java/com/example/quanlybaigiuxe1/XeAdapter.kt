package com.example.quanlybaigiuxe1

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class XeAdapter(private var danhSachXe: List<Xe>) : RecyclerView.Adapter<XeAdapter.XeViewHolder>() {

    class XeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBienSo: TextView = itemView.findViewById(R.id.tvBienSo)
        val tvLoaiXe: TextView = itemView.findViewById(R.id.tvLoaiXe)
        val tvGioVao: TextView = itemView.findViewById(R.id.tvGioVao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_xe, parent, false)
        return XeViewHolder(view)
    }

    override fun onBindViewHolder(holder: XeViewHolder, position: Int) {
        val xe = danhSachXe[position]
        holder.tvBienSo.text = xe.bienSo
        holder.tvLoaiXe.text = xe.loaiXe
        holder.tvGioVao.text = xe.gioVao

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChiTietXeActivity::class.java)
            intent.putExtra("BIEN_SO", xe.bienSo)
            intent.putExtra("LOAI_XE", xe.loaiXe)
            intent.putExtra("GIO_VAO", xe.gioVao)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return danhSachXe.size
    }

    fun updateData(newList: List<Xe>) {
        danhSachXe = newList
        notifyDataSetChanged()
    }
}