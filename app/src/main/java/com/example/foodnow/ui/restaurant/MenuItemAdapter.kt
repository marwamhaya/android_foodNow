package com.example.foodnow.ui.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.Constants
import com.example.foodnow.data.MenuItemResponse

class MenuItemAdapter(
    private var items: List<MenuItemResponse>,
    private val onItemClick: (MenuItemResponse) -> Unit,
    private val onEditClick: (MenuItemResponse) -> Unit,
    private val onDeleteClick: (MenuItemResponse) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMenuItemName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvMenuItemPrice)
        val tvDesc: TextView = itemView.findViewById(R.id.tvMenuItemDesc)
        val ivImage: android.widget.ImageView = itemView.findViewById(R.id.ivMenuItemImage)
        val btnDelete: android.widget.ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant_menu, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = "${item.price} DH"
        holder.tvDesc.text = item.description ?: ""
        
        if (!item.imageUrl.isNullOrEmpty()) {
            val fullUrl = if (item.imageUrl!!.startsWith("http")) item.imageUrl 
                           else "${Constants.BASE_URL}api/menu-items/images/${item.imageUrl}"
            com.bumptech.glide.Glide.with(holder.itemView.context).load(fullUrl).into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background) // Placeholder
        }

        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<MenuItemResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}
