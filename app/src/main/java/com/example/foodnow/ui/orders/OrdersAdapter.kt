package com.example.foodnow.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersAdapter(private var orders: List<Order>, private val onClick: (Order) -> Unit) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRestaurantName: TextView = view.findViewById(R.id.tvRestaurantName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvOrderItems: TextView = view.findViewById(R.id.tvOrderItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvRestaurantName.text = order.restaurantName ?: "FoodNow Order"
        holder.tvStatus.text = order.status
        
        // Simple date formatting (assuming ISO string from backend)
        holder.tvOrderDate.text = order.createdAt.take(10) 

        val itemCount = order.items.sumOf { it.quantity }
        holder.tvOrderItems.text = "$itemCount items • ${String.format("%.2f", order.totalAmount)} DH"

        holder.itemView.setOnClickListener { onClick(order) }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
