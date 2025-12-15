package com.example.foodnow.ui.restaurant

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import androidx.navigation.fragment.findNavController

class RestaurantDashboardFragment : Fragment(R.layout.fragment_restaurant_dashboard) {

    private val viewModel: RestaurantViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvRestaurantName)
        val tvAddress = view.findViewById<TextView>(R.id.tvRestaurantAddress)
        val tvStatus = view.findViewById<TextView>(R.id.tvRestaurantStatus)
        val tvDesc = view.findViewById<TextView>(R.id.tvRestaurantDescription)
        val btnRefresh = view.findViewById<Button>(R.id.btnRefresh)
        val ivImage = view.findViewById<android.widget.ImageView>(R.id.ivRestaurantImage)
        

        val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
        
        btnRefresh.setOnClickListener {
            viewModel.getMyRestaurant()
        }

        btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profile)
        }

        viewModel.restaurant.observe(viewLifecycleOwner) { result ->
            result.onSuccess { restaurant ->
                tvName.text = restaurant.name
                tvAddress.text = restaurant.address ?: "No address"
                tvStatus.text = if (restaurant.isActive) "Status: OPEN" else "Status: CLOSED"
                tvDesc.text = restaurant.description ?: "No description"
                
                // Load restaurant image
                if (!restaurant.imageUrl.isNullOrEmpty()) {
                    val fullUrl = if (restaurant.imageUrl.startsWith("http")) restaurant.imageUrl
                                   else "http://10.0.2.2:8080${restaurant.imageUrl}"
                    com.bumptech.glide.Glide.with(this)
                        .load(fullUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivImage)
                }
            }.onFailure {
                tvName.text = "Error loading restaurant"
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Initial Load
        viewModel.getMyRestaurant()
    }
}
