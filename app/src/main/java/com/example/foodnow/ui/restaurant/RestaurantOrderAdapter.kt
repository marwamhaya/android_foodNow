package com.example.foodnow.ui.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.Order

class RestaurantOrderAdapter(
    private var orders: List<Order>,
    private val onItemClick: (Order) -> Unit,
    private val onAction1Click: (Order) -> Unit,
    private val onAction2Click: (Order) -> Unit
) : RecyclerView.Adapter<RestaurantOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvPrice: TextView = itemView.findViewById(R.id.tvOrderPrice)
        val btnAction1: Button = itemView.findViewById(R.id.btnAction1)
        val btnAction2: Button = itemView.findViewById(R.id.btnAction2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvId.text = "Order ${order.id}"
        holder.tvStatus.text = order.status
        holder.tvPrice.text = "${String.format("%.2f", order.totalAmount)} DH"

        // Reset state for recycling
        holder.btnAction1.isEnabled = true
        holder.btnAction1.visibility = View.VISIBLE
        holder.btnAction2.isEnabled = true 
        holder.btnAction2.visibility = View.VISIBLE

        // Configure status background and buttons based on status
        when (order.status) {
            "PENDING" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.btnAction1.text = "Accept"
                holder.btnAction2.text = "Reject"
            }
            "ACCEPTED" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_accepted)
                holder.btnAction1.text = "Prepare"
                holder.btnAction2.visibility = View.GONE
            }
            "PREPARING" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_preparing)
                holder.btnAction1.text = "Ready"
                holder.btnAction2.visibility = View.GONE
            }
            "READY_FOR_PICKUP" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ready)
                holder.btnAction1.text = "Waiting for Pickup"
                holder.btnAction1.isEnabled = false
                holder.btnAction2.visibility = View.GONE
            }
            "DELIVERED" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ready)
                holder.btnAction1.visibility = View.GONE
                holder.btnAction2.visibility = View.GONE
            }
            "CANCELLED" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                holder.btnAction1.visibility = View.GONE
                holder.btnAction2.visibility = View.GONE
            }
            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_badge)
                holder.btnAction1.visibility = View.GONE
                holder.btnAction2.visibility = View.GONE
            }
        }

        holder.btnAction1.setOnClickListener { 
            android.widget.Toast.makeText(holder.itemView.context, "Clicked Action 1", android.widget.Toast.LENGTH_SHORT).show()
            onAction1Click(order) 
        }
        holder.btnAction2.setOnClickListener { 
            android.widget.Toast.makeText(holder.itemView.context, "Clicked Action 2", android.widget.Toast.LENGTH_SHORT).show()
            onAction2Click(order) 
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount() = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
