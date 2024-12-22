package com.chandratz.inventory

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.chandratz.inventory.databinding.FragmentRequestProductDialogBinding
import com.chandratz.inventory.model.RequestProduct
import com.chandratz.inventory.model.RequestProductResponse
import com.chandratz.myfirstapplication.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestProductDialogFragment : DialogFragment() {
    private var dismissListener: (() -> Unit)? = null
    private var _binding: FragmentRequestProductDialogBinding? = null
    private val binding get() = _binding!!

    fun setOnDismissListener(listener: () -> Unit) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestProductDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        binding.etTanggal.setText(dateFormat.format(today))

        // Disable editing tanggal
        binding.etTanggal.isEnabled = false

        binding.btnSubmitRequest.setOnClickListener {
            submitRequest()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun submitRequest() {
        val namaBarang = binding.etNamaBarang.text.toString()
        val jumlahPermintaan = binding.etJumlah.text.toString()
        val tanggal = binding.etTanggal.text.toString()

        if (namaBarang.isEmpty() || jumlahPermintaan.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(context, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val requestProduct = RequestProduct(
            nama_barang = namaBarang,
            jumlah_permintaan = jumlahPermintaan.toInt(),
            tanggal = tanggal
        )

        Log.d("API_DEBUG", "Request Body: $requestProduct")

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            val apiService = RetrofitClient.instance
            Log.d("API_DEBUG", "Token: Bearer $token")
            val call = apiService.postRequestProduct("Bearer $token", requestProduct)

            call.enqueue(object : Callback<RequestProductResponse> {
                override fun onResponse(
                    call: Call<RequestProductResponse>,
                    response: Response<RequestProductResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Permintaan berhasil dibuat", Toast.LENGTH_SHORT).show()
                        dismiss()

                        // Refresh data di RequestFragment
                        (parentFragment as? RequestFragment)?.fetchDataRequestProducts()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Error Body: $errorBody")
                        Log.e("API_ERROR", "Response Code: ${response.code()}")
                        Log.e("API", "Error: ${response.code()} ${response.message()}")
                        Toast.makeText(context, "Gagal membuat permintaan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RequestProductResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("API", "Failed: ${t.message}")
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}