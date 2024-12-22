package com.chandratz.inventory

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.chandratz.inventory.databinding.FragmentReportBinding
import com.chandratz.inventory.model.ReportData
import com.chandratz.myfirstapplication.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Calendar


class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ReportAdapter
    private var reportDataList: MutableList<ReportData> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListView()
        setupDatePickers()
        setupFilterButton()
        setupPrintButton()
    }

    private fun setupListView() {
        adapter = ReportAdapter(requireContext(), reportDataList)
        binding.requestListView.adapter = adapter
    }

    private fun setupDatePickers() {
        binding.startDate.setOnClickListener { showDatePicker { date -> binding.startDate.setText(date) } }
        binding.endDate.setOnClickListener { showDatePicker { date -> binding.endDate.setText(date) } }
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            val startDate = binding.startDate.text.toString()
            val endDate = binding.endDate.text.toString()

            if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                fetchReportData(startDate, endDate)
            } else {
                Toast.makeText(requireContext(), "Silakan isi tanggal mulai dan akhir.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPrintButton() {
        binding.btnPrint.setOnClickListener {
            val startDate = binding.startDate.text.toString()
            val endDate = binding.endDate.text.toString()

            if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                fetchAndDownloadPdf(startDate, endDate) // Mengunduh PDF
            } else {
                Toast.makeText(requireContext(), "Silakan pilih tanggal mulai dan akhir untuk mencetak laporan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun fetchReportData(startDate: String, endDate: String) {
        val apiService = RetrofitClient.instance

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            val call = apiService.getReportData("Bearer $token", startDate, endDate)
            call.enqueue(object : Callback<List<ReportData>> {
                override fun onResponse(call: Call<List<ReportData>>, response: Response<List<ReportData>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { reportResponse ->
                            reportDataList = reportResponse.toMutableList()
                            adapter.updateData(reportDataList)
                        } ?: run {
                            Log.e("ReportFragment", "Response body is null")
                        }
                    } else {
                        Log.e("ReportFragment", "Response not successful: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<ReportData>>, t: Throwable) {
                    Log.e("ReportFragment", "API call failed", t)
                }
            })
        } else {
            Log.e("ReportFragment", "Token is null")
        }
    }

    private fun fetchAndDownloadPdf(startDate: String, endDate: String) {
        // Memastikan ini dilakukan di background thread menggunakan coroutine
        lifecycleScope.launch {
            try {
                // Panggil API untuk mendownload PDF
                val response = withContext(Dispatchers.IO) {
                    val apiService = RetrofitClient.instance
                    val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val token = sharedPreferences.getString("auth_token", null)

                    if (token != null) {
                        apiService.downloadPdf("Bearer $token", startDate, endDate).execute()
                    } else {
                        null
                    }
                }

                if (response != null && response.isSuccessful) {
                    // Simpan PDF ke file setelah berhasil mendownload
                    savePdfToFile(response.body()?.byteStream())
                } else {
                    Log.e("ReportFragment", "PDF download failed or token is null")
                }

            } catch (e: Exception) {
                // Tangani kesalahan yang terjadi
                Log.e("ReportFragment", "Error while downloading PDF", e)
            }
        }
    }

    private fun savePdfToFile(inputStream: InputStream?) {
        lifecycleScope.launch(Dispatchers.IO) { // pindahkan ke background thread
            try {
                if (inputStream != null) {
                    // Tentukan lokasi penyimpanan file PDF
                    val file = File(requireContext().getExternalFilesDir(null), "report.pdf")
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    outputStream.close()

                    Log.d("ReportFragment", "PDF saved successfully at: ${file.absolutePath}")
                    Log.d("ReportFragment", "File exists: ${file.exists()}")
                    Log.d("ReportFragment", "File size: ${file.length()}")

                    // Setelah berhasil menyimpan, panggil fungsi untuk membuka PDF di thread utama
                    withContext(Dispatchers.Main) {
                        openPdf(file)
                    }
                } else {
                    Log.e("ReportFragment", "Input stream is null, can't save PDF")
                }
            } catch (e: Exception) {
                Log.e("ReportFragment", "Error saving PDF file", e)
            }
        }
    }


    private fun openPdf(file: File) {
        try {
            val pdfUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            // Mencoba membuka dengan Adobe Acrobat terlebih dahulu
            val adobeIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                setPackage("com.google.android.apps.docs") // Package name Adobe Acrobat
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                startActivity(adobeIntent)
            } catch (e: Exception) {
                val installIntent = Intent(Intent.ACTION_VIEW, Uri.parse("drive.google.com"))
                startActivity(installIntent)
            }

        } catch (e: Exception) {
            Log.e("ReportFragment", "Error opening PDF file", e)
            Toast.makeText(requireContext(), "Error opening PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
