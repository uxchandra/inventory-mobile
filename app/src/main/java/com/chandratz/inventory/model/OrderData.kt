package com.chandratz.inventory.model

import java.util.Date

data class OrderData(
    val id: Int,
    val nama_barang: String,
    val jumlah_permintaan: Int,
    val tanggal: Date,
    val status: String
)
