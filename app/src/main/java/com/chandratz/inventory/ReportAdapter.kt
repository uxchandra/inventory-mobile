package com.chandratz.inventory

import android.content.Context
import android.text.method.TextKeyListener.clear
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.chandratz.inventory.model.ReportData
import java.util.Collections.addAll

class ReportAdapter(private val context: Context, private var reportList: List<ReportData>) :
    ArrayAdapter<ReportData>(context, 0, reportList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val report = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_report, parent, false)

        val textTanggal = view.findViewById<TextView>(R.id.textTanggal)
        val textName = view.findViewById<TextView>(R.id.textName)
        val textQuantity = view.findViewById<TextView>(R.id.textQuantity)

        report?.let {
            textTanggal.text = it.tanggal
            textName.text = it.nama_barang
            textQuantity.text = "Jumlah: ${it.jumlah_permintaan}"
        }

        return view
    }

    fun updateData(newReportList: List<ReportData>) {
        reportList = newReportList
        clear()
        addAll(reportList)
        notifyDataSetChanged()
    }
}
