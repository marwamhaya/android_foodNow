package com.example.foodnow.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodnow.R
import com.example.foodnow.data.Constants
import com.example.foodnow.data.MenuItemResponse
import java.text.NumberFormat
import java.util.Locale

class MenuAdapter(
    private var menuItems: List<MenuItemResponse>,
    private val onItemClick: (MenuItemResponse) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDish: ImageView = itemView.findViewById(R.id.ivDish)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]
        holder.tvName.text = item.name
        holder.tvDescription.text = item.description ?: ""
        holder.tvPrice.text = "${String.format("%.2f", item.price)} DH"

        if (!item.imageUrl.isNullOrEmpty()) {
            val fullUrl = if (item.imageUrl.startsWith("http")) item.imageUrl 
                           else "${Constants.BASE_URL}${item.imageUrl}"
            Glide.with(holder.itemView.context)
                .load(fullUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivDish)
        } else {
            holder.ivDish.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = menuItems.size

    fun updateData(newItems: List<MenuItemResponse>) {
        menuItems = newItems
        notifyDataSetChanged()
    }
}
