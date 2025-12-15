package com.example.foodnow.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.LivreurResponse

class AdminLivreurAdapter(
    private var livreurs: List<LivreurResponse>,
    private val onToggleClick: (LivreurResponse) -> Unit,
    private val onItemClick: (LivreurResponse) -> Unit
) : RecyclerView.Adapter<AdminLivreurAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvLivreurName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvLivreurStatus)
        val btnToggle: Button = itemView.findViewById(R.id.btnToggleStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_livreur, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = livreurs[position]
        // item.fullName might not exist on LivreurResponse, it has fullName directly
        holder.tvName.text = "${item.fullName} (${item.vehicleType})"
        holder.tvStatus.text = if (item.isActive) "Active" else "Inactive"
        holder.btnToggle.text = if (item.isActive) "Disable" else "Enable"
        
        holder.btnToggle.setOnClickListener { onToggleClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = livreurs.size

    fun updateData(newData: List<LivreurResponse>) {
        livreurs = newData
        notifyDataSetChanged()
    }
}
