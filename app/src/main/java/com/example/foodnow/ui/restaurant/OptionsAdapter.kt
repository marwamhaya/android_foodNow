package com.example.foodnow.ui.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.MenuOptionResponse

class OptionsAdapter : ListAdapter<MenuOptionResponse, OptionsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(item: MenuOptionResponse) {
            text1.text = item.name
            text2.text = "+ ${item.extraPrice} DH"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MenuOptionResponse>() {
        override fun areItemsTheSame(oldItem: MenuOptionResponse, newItem: MenuOptionResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MenuOptionResponse, newItem: MenuOptionResponse): Boolean {
            return oldItem == newItem
        }
    }
}
