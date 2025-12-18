package com.example.foodnow.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.Order
import com.example.foodnow.FoodNowApp
import com.example.foodnow.data.Constants
import com.example.foodnow.data.OrderItem
import com.example.foodnow.databinding.BottomSheetOrderDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.navigation.fragment.findNavController
import com.example.foodnow.ui.orders.TrackOrderFragment
import com.bumptech.glide.Glide
import java.math.BigDecimal
import java.util.Locale

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

        // Handle Track Button visibility
        if (order.status == "IN_DELIVERY" || order.status == "ON_THE_WAY" || order.status == "PICKED_UP") {
            binding.btnTrackOrder.visibility = View.VISIBLE
            binding.btnTrackOrder.setOnClickListener {
                dismiss()
                val bundle = Bundle().apply { putLong("orderId", order.id) }
                try {
                    findNavController().navigate(R.id.nav_track_order, bundle)
                } catch (e: Exception) {
                    // Fallback
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, TrackOrderFragment().apply { arguments = bundle })
                        .addToBackStack(null)
                        .commit()
                }
            }
        } else {
             binding.btnTrackOrder.visibility = View.GONE
        }
        
        binding.btnClose.setOnClickListener { dismiss() }
    }
}

class OrderItemsAdapter(private val items: List<OrderItem>) : RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage)
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvItemDetails: TextView = view.findViewById(R.id.tvItemDetails)
        val tvPriceCalculation: TextView = view.findViewById(R.id.tvPriceCalculation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        // Name
        holder.tvItemName.text = item.menuItemName
        
        // Image
        if (!item.menuItemImageUrl.isNullOrEmpty()) {
            val fullUrl = if (item.menuItemImageUrl.startsWith("http")) item.menuItemImageUrl 
                           else "${Constants.BASE_URL}api/menu-items/images/${item.menuItemImageUrl}"
            Glide.with(holder.itemView.context).load(fullUrl).into(holder.ivItemImage)
        } else {
            holder.ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        
        // Price Calculation: "95,00 DH × 2 = 190,00 DH"
        val unitPrice = item.price
        val quantity = item.quantity
        val total = unitPrice.multiply(BigDecimal(quantity))
        
        val unitPriceStr = String.format(Locale.FRANCE, "%.2f", unitPrice)
        val totalStr = String.format(Locale.FRANCE, "%.2f", total)
        
        holder.tvPriceCalculation.text = "$totalStr DH (Qty: $quantity)"
        
        // Show supplements/options if any
        if (!item.selectedOptions.isNullOrEmpty()) {
            holder.tvItemDetails.visibility = View.VISIBLE
            val supplementsText = item.selectedOptions.joinToString("\n") { 
                val optPriceStr = String.format(Locale.FRANCE, "%.2f", it.price)
                "+ ${it.name}: +$optPriceStr DH" 
            }
            holder.tvItemDetails.text = supplementsText
        } else {
            holder.tvItemDetails.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}
