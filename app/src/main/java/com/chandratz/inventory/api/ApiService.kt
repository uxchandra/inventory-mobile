package com.chandratz.myfirstapplication.api


import com.chandratz.inventory.model.DashboardResponse
import com.chandratz.inventory.model.OrderResponse
import com.chandratz.inventory.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String, val data: UserData?)
data class UserData(val user: User, val token: String)

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("dashboard")
    fun getDashboardData(@Header("Authorization") token: String): Call<DashboardResponse>

    @GET("permintaan-produk/get-data")
    fun getDataRequestProducts(@Header("Authorization") token: String): Call<OrderResponse>
}