package com.example.quanlybaigiuxe1

data class Ticket(
    val id: Int,
    val plate: String,
    val type: String,
    val time_in: String,
    val time_out: String?,
    val price: Int,
    val status: Int
)