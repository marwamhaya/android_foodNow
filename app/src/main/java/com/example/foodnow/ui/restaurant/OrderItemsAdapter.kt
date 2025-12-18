package com.example.foodnow.ui.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodnow.R
import com.example.foodnow.data.Constants
import com.example.foodnow.data.OrderItem
import java.math.BigDecimal
import java.util.Locale

class OrderItemsAdapter(private val items: List<OrderItem>) : 
    RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage)
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvPriceCalculation: TextView = view.findViewById(R.id.tvPriceCalculation)
        val tvOptions: TextView = view.findViewById(R.id.tvItemDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        // Name
        holder.tvName.text = item.menuItemName
        
        // Image
        if (!item.menuItemImageUrl.isNullOrEmpty()) {
            val fullUrl = if (item.menuItemImageUrl!!.startsWith("http")) item.menuItemImageUrl 
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
        
        // Supplements/Options
        val options = item.selectedOptions
        if (!options.isNullOrEmpty()) {
            val supplementsText = options.joinToString("\n") { 
                val optPriceStr = String.format(Locale.FRANCE, "%.2f", it.price)
                "+ ${it.name}: +$optPriceStr DH" 
            }
            holder.tvOptions.text = supplementsText
            holder.tvOptions.visibility = View.VISIBLE
        } else {
            holder.tvOptions.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}
