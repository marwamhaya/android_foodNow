package com.example.foodnow.ui.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.MenuOptionGroupResponse

class OptionGroupAdapter(private val onClick: (MenuOptionGroupResponse) -> Unit) : 
    ListAdapter<MenuOptionGroupResponse, OptionGroupAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvGroupName)
        private val tvRequired: TextView = itemView.findViewById(R.id.tvGroupRequired)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvGroupDetails)

        fun bind(item: MenuOptionGroupResponse) {
            tvName.text = item.name
            
            if (item.isRequired) {
                tvRequired.visibility = View.VISIBLE
                tvRequired.text = "Required"
            } else {
                tvRequired.visibility = View.GONE
            }
            
            val type = if (item.isMultiple) "Multiple Choice" else "Single Choice"
            tvDetails.text = "$type • ${item.options.size} Options"
            
            itemView.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MenuOptionGroupResponse>() {
        override fun areItemsTheSame(oldItem: MenuOptionGroupResponse, newItem: MenuOptionGroupResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MenuOptionGroupResponse, newItem: MenuOptionGroupResponse): Boolean {
            return oldItem == newItem
        }
    }
}
