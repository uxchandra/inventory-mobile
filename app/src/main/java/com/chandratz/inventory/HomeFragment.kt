package com.chandratz.inventory

import android.content.Context
import android.content.Intent
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

        // Set OnClickListener pada CircleImageView
        binding.photoImageView.setOnClickListener {
            toggleLogoutCardVisibility()
        }

        // Set OnClickListener untuk button logout
        binding.logoutButton.setOnClickListener {
            logout()
        }

        fetchDashboardData()
    }

    // Fungsi untuk mengontrol visibilitas CardView logout
    private fun toggleLogoutCardVisibility() {
        val logoutCard = binding.logoutCard
        if (logoutCard.visibility == View.GONE) {
            logoutCard.visibility = View.VISIBLE
        } else {
            logoutCard.visibility = View.GONE
        }
    }

    // Fungsi untuk logout
    private fun logout() {
        // Menghapus token dari SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("auth_token")  // Menghapus token
        editor.apply()

        // Mengarahkan pengguna ke halaman login
        val loginIntent = Intent(activity, MainActivity::class.java)
        startActivity(loginIntent)
        requireActivity().finish()
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
                    if (response.isSuccessful) {
                        response.body()?.let { dashboardResponse ->
                            val dashboardData = dashboardResponse.data
                            binding.tvJumlahPermintaan.text = dashboardData?.jumlahPermintaan.toString()
                            dashboardData?.let {
                                setupPieChart(it.barangPalingBanyakDiminta)
                            }
                        }
                    } else {
                        Log.e("Dashboard", "Response not successful: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                    Log.e("Dashboard", "API call failed", t)
                }
            })
        }
    }

    private fun setupPieChart(barangData: List<BarangData>) {
        val entries = ArrayList<PieEntry>()
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


