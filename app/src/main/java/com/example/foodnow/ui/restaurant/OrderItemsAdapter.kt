package com.example.foodnow.ui.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.OrderItem

class OrderItemsAdapter(private val items: List<OrderItem>) : 
    RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvOptions: TextView = view.findViewById(R.id.tvItemDetails)
        val tvPrice: TextView = view.findViewById(R.id.tvItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvQuantity.text = "${item.quantity}x"
        holder.tvName.text = item.menuItemName
        holder.tvPrice.text = "${item.price * java.math.BigDecimal(item.quantity)} DH"
        
        // Display selected options if any
        val options = item.selectedOptions
        if (!options.isNullOrEmpty()) {
            val optionsText = options.joinToString(", ") { "${it.name} (+${it.price} DH)" }
            holder.tvOptions.text = "→ $optionsText"
            holder.tvOptions.visibility = View.VISIBLE
        } else {
            holder.tvOptions.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}
