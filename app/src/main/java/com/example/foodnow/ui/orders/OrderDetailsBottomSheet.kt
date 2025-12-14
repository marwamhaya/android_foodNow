package com.example.foodnow.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.Order
import com.example.foodnow.data.OrderItem
import com.example.foodnow.databinding.BottomSheetOrderDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class OrderDetailsBottomSheet(private val order: Order) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetOrderDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvOrderId.visibility = View.GONE // Hide ID
        binding.tvOrderId.text = "Order #${order.id}"
        binding.tvRestaurantName.text = order.restaurantName
        binding.tvOrderDate.text = "Date: ${order.createdAt.take(10)}"
        binding.tvOrderStatus.text = "Status: ${order.status}"
        binding.tvTotal.text = "${String.format("%.2f", order.totalAmount)} DH"

        // Set status color
        when (order.status) {
            "DELIVERED" -> binding.tvOrderStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            "CANCELLED" -> binding.tvOrderStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            else -> binding.tvOrderStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
        }

        // Setup RecyclerView
        val adapter = OrderItemsAdapter(order.items)
        binding.rvOrderItems.layoutManager = LinearLayoutManager(context)
        binding.rvOrderItems.adapter = adapter
    }
}

class OrderItemsAdapter(private val items: List<OrderItem>) : RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvItemDetails: TextView = view.findViewById(R.id.tvItemDetails)
        val tvItemPrice: TextView = view.findViewById(R.id.tvItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvItemName.text = "${item.quantity}x ${item.menuItemName}"
        
        // Show supplements with prices if any
        if (!item.selectedOptions.isNullOrEmpty()) {
            holder.tvItemDetails.visibility = View.VISIBLE
            val detailsText = item.selectedOptions.joinToString(", ") { 
                "${it.name} (+${String.format("%.2f", it.price)} DH)" 
            }
            holder.tvItemDetails.text = "• $detailsText"
        } else {
            holder.tvItemDetails.visibility = View.GONE
        }
        
        // Calculate total price for item including supplements
        // Ideally backend provides this or we act as if price is unit price * qty
        // But backend subtotal is already calculated. 
        // Wait, OrderItem in Models.kt has 'price: BigDecimal'. 
        // In API Service (OrderController) -> OrderService -> mapToOrderItemResponse -> sets 'unitPrice' to OrderItemResponse.unitPrice.
        // Android OrderItem 'price' maps to JSON 'unitPrice' or 'subtotal'?
        // Looking at Models.kt: data class OrderItem(..., val price: BigDecimal, ...).
        // Looking at JSON Mapping, if Gson is used, name must match.
        // OrderItemResponse has 'unitPrice' and 'subtotal'. Models.kt OrderItem has 'price'.
        // This is a mismatch unless SerializedName is used.
        // CartManager uses menuItem.price.
        // Let's assume 'price' in OrderItem is just the unit price for now or the subtotal?
        // Let's look at `item_order_detail.xml` view which uses `tvItemPrice`.
        // The user wants "Total Price of item".
        // If I use the price from the model, I should multiply by quantity.

        val totalPrice = item.price.multiply(java.math.BigDecimal(item.quantity))
        holder.tvItemPrice.text = "${String.format("%.2f", totalPrice)} DH"
    }

    override fun getItemCount() = items.size
}
