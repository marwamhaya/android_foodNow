package com.example.foodnow.ui.livreur

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.DeliveryResponse

class DeliveryAdapter(
    private var deliveries: List<DeliveryResponse>,
    private val onActionClick: (DeliveryResponse) -> Unit
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvDeliveryId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvDeliveryStatus)
        val tvResto: TextView = itemView.findViewById(R.id.tvRestaurantInfo)
        val tvClient: TextView = itemView.findViewById(R.id.tvClientInfo)
        val btnAction: Button = itemView.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_delivery, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val item = deliveries[position]
        holder.tvId.text = "Delivery #${item.id}"
        holder.tvStatus.text = "Status: ${item.status}"
        holder.tvResto.text = "From: ${item.restaurantName}\n${item.restaurantAddress}"
        holder.tvClient.text = "To: ${item.clientName}\n${item.clientAddress}"

        when (item.status) {
            "PENDING" -> {
                holder.btnAction.text = "Accept"
                holder.btnAction.visibility = View.VISIBLE
            }
            "DELIVERY_ACCEPTED" -> {
                holder.btnAction.text = "Pick Up"
                holder.btnAction.visibility = View.VISIBLE
                
                // Also could open map here to navigate to restaurant
                holder.itemView.setOnClickListener { onActionClick(item) } 
            }
            "PICKED_UP" -> { 
                holder.btnAction.text = "Open Active Map"
                holder.btnAction.visibility = View.VISIBLE
            }
            "READY_FOR_PICKUP" -> {
                holder.btnAction.text = "Pick Up"
                holder.btnAction.visibility = View.VISIBLE
            }
            "IN_DELIVERY", "ON_THE_WAY" -> {
                holder.btnAction.text = "Open Active Map"
                holder.btnAction.visibility = View.VISIBLE
            }
            "DELIVERED" -> {
                holder.btnAction.visibility = View.GONE
            }
            else -> {
                holder.btnAction.visibility = View.GONE
            }
        }

        holder.btnAction.setOnClickListener { onActionClick(item) }
    }

    override fun getItemCount() = deliveries.size

    fun updateDeliveries(newItems: List<DeliveryResponse>) {
        deliveries = newItems
        notifyDataSetChanged()
    }
}
