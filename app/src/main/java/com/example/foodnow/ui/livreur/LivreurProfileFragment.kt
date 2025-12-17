package com.example.foodnow.ui.livreur

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodnow.FoodNowApp
import com.example.foodnow.MainActivity
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory

class LivreurProfileFragment : Fragment(R.layout.fragment_livreur_profile) {

    private val viewModel: LivreurViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tvName = view.findViewById<TextView>(R.id.tvLivreurName)
        val tvStatus = view.findViewById<TextView>(R.id.tvLivreurStatus)
        val btnToggle = view.findViewById<Button>(R.id.btnToggleAvailability)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        
        btnToggle.setOnClickListener { viewModel.toggleAvailability() }

        btnLogout.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        
        viewModel.profile.observe(viewLifecycleOwner) { result ->
            result.onSuccess { profile ->
                tvName.text = profile.fullName
                tvStatus.text = if (profile.isAvailable) "Status: AVAILABLE" else "Status: OFFLINE"
                btnToggle.text = if (profile.isAvailable) "Go Offline" else "Go Online"
            }
        }
        viewModel.getProfile()
    }
}
