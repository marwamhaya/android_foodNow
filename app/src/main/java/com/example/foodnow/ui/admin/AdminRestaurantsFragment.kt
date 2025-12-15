package com.example.foodnow.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminRestaurantsFragment : Fragment(R.layout.fragment_admin_restaurants) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }
    private lateinit var adapter: AdminRestaurantAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvRestaurants)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddRestaurant)

        rv.layoutManager = LinearLayoutManager(context)
        adapter = AdminRestaurantAdapter(emptyList(), 
            onToggleClick = { item -> viewModel.toggleRestaurantStatus(item.id) },
            onItemClick = { item -> 
                val bundle = Bundle().apply { putLong("restaurantId", item.id) }
                findNavController().navigate(R.id.action_admin_restaurants_to_details, bundle)
            }
        )
        rv.adapter = adapter

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_admin_restaurants_to_create)
        }

        viewModel.restaurants.observe(viewLifecycleOwner) { result ->
            result.onSuccess { list -> adapter.updateData(list) }
            result.onFailure { e -> 
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.getAllRestaurants()
    }
}
