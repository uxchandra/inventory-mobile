package com.chandratz.myfirstapplication.api


import com.chandratz.inventory.model.DashboardResponse
import com.chandratz.inventory.model.OrderResponse
import com.chandratz.inventory.model.ReportData
import com.chandratz.inventory.model.RequestProduct
import com.chandratz.inventory.model.RequestProductResponse
import com.chandratz.inventory.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming


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

    @POST("permintaan-produk")
    fun postRequestProduct(
        @Header("Authorization") token: String,
        @Body request: RequestProduct
    ): Call<RequestProductResponse>

    @GET("laporan-permintaan/get-data")
    fun getReportData(
        @Header("Authorization") token: String,
        @Query("tanggal_mulai") startDate: String,
        @Query("tanggal_selesai") endDate: String
    ): Call<List<ReportData>>

    @GET("laporan-permintaan/print")
    @Streaming
    fun downloadPdf(
        @Header("Authorization") authHeader: String,
        @Query("tanggal_mulai") startDate: String,
        @Query("tanggal_selesai") endDate: String
    ): Call<ResponseBody>
}