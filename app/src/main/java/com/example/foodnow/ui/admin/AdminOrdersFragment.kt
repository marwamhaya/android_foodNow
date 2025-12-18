package com.example.foodnow.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
// Reuse RestaurantOrderAdapter? Yes, if it takes Order list.
import com.example.foodnow.ui.restaurant.RestaurantOrderAdapter

class AdminOrdersFragment : Fragment(R.layout.fragment_restaurant_orders) { // Reuse layout

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }
    private lateinit var adapter: RestaurantOrderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val restaurantId = arguments?.getLong("restaurantId") ?: return
        val rv = view.findViewById<RecyclerView>(R.id.rvOrders)
        
        rv.layoutManager = LinearLayoutManager(context)
        adapter = RestaurantOrderAdapter(emptyList(), {}, {}, {}) // Actions disabled for Admin view only? Or allowed?
        // If Admin can manage orders, pass actions. 
        // For now, pass empty lambdas (View Only).
        rv.adapter = adapter
        
        viewModel.restaurantOrders.observe(viewLifecycleOwner) { result ->
            result.onSuccess { list -> adapter.updateOrders(list) }
        }
        viewModel.getRestaurantOrders(restaurantId)
    }
}
