package com.chandratz.inventory

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chandratz.inventory.databinding.FragmentHomeBinding
import com.chandratz.inventory.model.BarangData
import com.chandratz.inventory.model.DashboardResponse
import com.chandratz.myfirstapplication.api.ApiService
import com.chandratz.myfirstapplication.api.RetrofitClient
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        val apiService = RetrofitClient.instance

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            val call = apiService.getDashboardData("Bearer $token")

            call.enqueue(object : Callback<DashboardResponse> {
                override fun onResponse(
                    call: Call<DashboardResponse>,
                    response: Response<DashboardResponse>
                ) {
                    // Log respons mentah sebelum parsing
                    Log.d("Dashboard", "Raw response: ${response.raw().message}")

                    if (response.isSuccessful) {
                        response.body()?.let { dashboardResponse ->
                            val dashboardData = dashboardResponse.data

                            binding.tvJumlahPermintaan.text = dashboardData?.jumlahPermintaan.toString()

                            if (dashboardData != null) {
                                setupPieChart(dashboardData.barangPalingBanyakDiminta)
                            }

                            Log.d("Dashboard", "Data received: $dashboardData")
                        } ?: run {
                            Log.e("Dashboard", "Response body is null")
                        }
                    } else {
                        Log.e("Dashboard", "Response not successful: ${response.code()}")
                        response.errorBody()?.let { errorBody ->
                            Log.d("Dashboard", "Error response: ${errorBody.string()}")
                        }
                    }
                }

                override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                    Log.e("Dashboard", "API call failed", t)
                }
            })
        } else {
            Log.e("Dashboard", "Token is null")
        }
    }


    private fun setupPieChart(barangData: List<BarangData>) {
        val entries = ArrayList<PieEntry>()

        // Menghitung total dari semua barang untuk perhitungan persen
        val total = barangData.sumBy { it.total }

        for (barang in barangData) {
            entries.add(PieEntry(barang.total.toFloat(), barang.nama_barang))
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.BLACK
            setDrawValues(true)
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f%%", (value / total) * 100)
                }
            })
        }

        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 10f
            setEntryLabelColor(Color.TRANSPARENT)
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
