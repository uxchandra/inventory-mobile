package com.chandratz.inventory

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chandratz.inventory.databinding.FragmentRequestBinding
import com.chandratz.inventory.model.OrderData
import com.chandratz.inventory.model.OrderResponse
import com.chandratz.myfirstapplication.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestFragment : Fragment() {
    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RequestAdapter
    private var orderDataList: MutableList<OrderData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListView()
        fetchDataRequestProducts()
    }

    private fun setupListView() {
        adapter = RequestAdapter(requireContext(), orderDataList)
        binding.requestListView.adapter = adapter
    }

    private fun fetchDataRequestProducts() {
        val apiService = RetrofitClient.instance

        // Ambil token dari SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        // Memastikan token tidak null
        if (token != null) {
            val call = apiService.getDataRequestProducts("Bearer $token")
            call.enqueue(object : Callback<OrderResponse> {
                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { orderResponse ->
                            orderDataList = orderResponse.data.toMutableList()
                            adapter.updateData(orderDataList)
                        } ?: run {
                            Log.e("RequestFragment", "Response body is null")
                        }
                    } else {
                        Log.e("RequestFragment", "Response not successful: ${response.code()}")
                        response.errorBody()?.let { errorBody ->
                            Log.d("RequestFragment", "Error response: ${errorBody.string()}")
                        }
                    }
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    Log.e("RequestFragment", "API call failed", t)
                }
            })
        } else {
            Log.e("RequestFragment", "Token is null")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
