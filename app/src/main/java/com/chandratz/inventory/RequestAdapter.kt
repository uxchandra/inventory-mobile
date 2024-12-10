package com.chandratz.inventory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chandratz.inventory.model.OrderData
import java.text.SimpleDateFormat
import java.util.Locale

class RequestAdapter(private val context: Context, private var orderList: List<OrderData>) : ArrayAdapter<OrderData>(context, 0, orderList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val order = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_request, parent, false)

        val textTanggal = view.findViewById<TextView>(R.id.textTanggal)
        val textName = view.findViewById<TextView>(R.id.textName)
        val textQuantity = view.findViewById<TextView>(R.id.textQuantity)
        val textStatus = view.findViewById<TextView>(R.id.textStatus)

        order?.let {
            textTanggal.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it.tanggal)
            textName.text = it.nama_barang
            textQuantity.text = "Jumlah Permintaan: ${it.jumlah_permintaan}"
            textStatus.text = it.status

            when (it.status) {
                "menunggu_konfirmasi" -> textStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow))
                "diterima" -> textStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                "ditolak" -> textStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                "selesai" -> textStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.blue))
                else -> textStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.gray))
            }
        }

        return view
    }

    fun updateData(newOrderList: List<OrderData>) {
        orderList = newOrderList
        clear()
        addAll(orderList)
        notifyDataSetChanged()
    }
}
