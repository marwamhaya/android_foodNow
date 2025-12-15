package com.example.foodnow.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import com.example.foodnow.data.User

class AdminUsersFragment : Fragment(R.layout.fragment_admin_users) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    private lateinit var adapter: AdminUsersAdapter
    private lateinit var progressBar: ProgressBar

    private var allUsers: List<User> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        val rvUsers = view.findViewById<RecyclerView>(R.id.rvUsers)
        val spinnerFilter = view.findViewById<android.widget.Spinner>(R.id.spinnerRoleFilter)
        
        // Setup Spinner
        val roles = listOf("ALL", "CLIENT", "RESTAURANT", "LIVREUR", "ADMIN")
        val spinnerAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter
        
        spinnerFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRole = roles[position]
                filterUsers(selectedRole)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        rvUsers.layoutManager = LinearLayoutManager(context)

        adapter = AdminUsersAdapter(emptyList(), 
            onToggleStatus = { user ->
                showConfirmationDialog("Toggle Status", "Are you sure you want to change status for ${user.fullName}?") {
                    viewModel.toggleUserStatus(user.id)
                }
            },
            onResetPassword = { user ->
                showResetPasswordDialog(user)
            }
        )
        rvUsers.adapter = adapter

        viewModel.users.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            result.onSuccess { users ->
                allUsers = users
                filterUsers(spinnerFilter.selectedItem.toString())
            }
            result.onFailure {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        loadUsers()
    }
    
    private fun filterUsers(role: String) {
        val filtered = if (role == "ALL") {
            allUsers
        } else {
            allUsers.filter { it.role.toString() == role }
        }
        adapter.updateList(filtered)
    }

    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        viewModel.getAllUsers()
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showResetPasswordDialog(user: User) {
        val input = EditText(requireContext())
        input.hint = "New Password"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Password")
            .setMessage("Enter new password for ${user.fullName}")
            .setView(input)
            .setPositiveButton("Reset") { _, _ ->
                val newPass = input.text.toString()
                if (newPass.isNotEmpty()) {
                    viewModel.resetUserPassword(user.id, newPass, 
                        onSuccess = { Toast.makeText(context, "Password Reset Successful", Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
