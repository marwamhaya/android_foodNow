package com.example.foodnow.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory

class AdminRestaurantDetailsFragment : Fragment(R.layout.fragment_admin_restaurant_details) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurantId = arguments?.getLong("restaurantId") ?: return

        val ivImage = view.findViewById<ImageView>(R.id.ivRestoDetailImage)
        val tvName = view.findViewById<TextView>(R.id.tvRestoDetailName)
        val tvAddress = view.findViewById<TextView>(R.id.tvRestoDetailAddress)
        val tvPhone = view.findViewById<TextView>(R.id.tvRestoDetailPhone)
        val tvDesc = view.findViewById<TextView>(R.id.tvRestoDetailDesc)
        val tvOwner = view.findViewById<TextView>(R.id.tvRestoDetailOwnerName)
        val btnEdit = view.findViewById<Button>(R.id.btnEditRestaurant)
        val tvDailyOrders = view.findViewById<TextView>(R.id.tvDailyOrderCount)

        viewModel.getRestaurantById(restaurantId)
        viewModel.getDailyOrderCount(restaurantId)

        viewModel.restaurantDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { resto ->
                tvName.text = resto.name
                tvAddress.text = "Address: ${resto.address}"
                tvPhone.text = "Phone: ${resto.phone}"
                tvDesc.text = resto.description
                tvOwner.text = "Owner: ${resto.ownerName ?: "Unknown"}" // Handle nullable ownerName

                if (!resto.imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(resto.imageUrl).centerCrop().into(ivImage)
                }
            }
            result.onFailure {
                Toast.makeText(context, "Error loading details: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.dailyOrderCount.observe(viewLifecycleOwner) { result ->
            result.onSuccess { count ->
                tvDailyOrders.text = count.toString()
            }
            result.onFailure {
                tvDailyOrders.text = "-"
            }
        }

        btnEdit.setOnClickListener {
             val bundle = Bundle().apply { putLong("restaurantId", restaurantId) }
             findNavController().navigate(R.id.action_details_to_edit, bundle)
        }
    }
}
