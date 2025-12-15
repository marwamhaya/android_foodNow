package com.example.foodnow.ui.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.data.User

class AdminUsersAdapter(
    private var users: List<User>,
    private val onToggleStatus: (User) -> Unit,
    private val onResetPassword: (User) -> Unit
) : RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvRole: TextView = itemView.findViewById(R.id.tvUserRole)
        val tvStatus: TextView = itemView.findViewById(R.id.tvUserStatus)
        val btnToggle: Button = itemView.findViewById(R.id.btnToggleStatus)
        val btnReset: Button = itemView.findViewById(R.id.btnResetPassword)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.fullName
        holder.tvEmail.text = user.email
        holder.tvRole.text = user.role.toString()
        
        if (user.isActive) {
            holder.tvStatus.text = "Active"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            holder.btnToggle.text = "Suspend"
        } else {
            holder.tvStatus.text = "Suspended"
            holder.tvStatus.setTextColor(Color.RED)
            holder.btnToggle.text = "Activate"
        }

        holder.btnToggle.setOnClickListener { onToggleStatus(user) }
        holder.btnReset.setOnClickListener { onResetPassword(user) }
    }

    override fun getItemCount() = users.size

    fun updateList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }
}
