package com.example.foodnow.ui.restaurant

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {

    private val viewModel: RestaurantViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etCurrent = view.findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = view.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnSave = view.findViewById<Button>(R.id.btnChangePassword)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Setup generic status observer (since we might reuse one or add specific one)
        // Ideally ViewModel should have explicit ChangePassword status.
        // For now, let's inject logic here or update ViewModel again.
        
        btnSave.setOnClickListener {
            val current = etCurrent.text.toString()
            val newPass = etNew.text.toString()
            val confirm = etConfirm.text.toString()

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Trigger ViewModel
            // We need a way to observe success.
            // Let's add specific LiveData to ViewModel or use a callback mechanism if simple.
            // Or assume fire-and-forget for now? No, we need feedback.
            // I'll update ViewModel to expose 'passwordChangeStatus'.
            viewModel.changePassword(current, newPass)
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false
        }
        
        // Observe status
        viewModel.passwordChangeStatus.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            btnSave.isEnabled = true
            result.onSuccess {
                Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }.onFailure {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        btnSave.setOnClickListener {
            val current = etCurrent.text.toString()
            val newPass = etNew.text.toString()
            val confirm = etConfirm.text.toString()

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.changePassword(current, newPass)
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false
        }
    }
}
