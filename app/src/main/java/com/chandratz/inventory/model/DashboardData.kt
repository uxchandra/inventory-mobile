package com.chandratz.inventory.model

data class DashboardData(
    val jumlahPermintaan: Int,
    val barangPalingBanyakDiminta: List<BarangData>,
    val chart: ChartData
)
