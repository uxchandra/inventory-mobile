package com.chandratz.inventory.model

data class DashboardResponse(
    val status: String,
    val data: DashboardData?,
    val message: String
)
