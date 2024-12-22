package com.chandratz.inventory.model

data class RequestProduct(
    val nama_barang: String,
    val jumlah_permintaan: Int,
    val tanggal: String
)
