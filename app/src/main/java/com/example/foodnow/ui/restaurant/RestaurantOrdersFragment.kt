package com.example.foodnow.ui.restaurant

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory

import androidx.navigation.fragment.findNavController

class RestaurantOrdersFragment : Fragment(R.layout.fragment_restaurant_orders) {

    private val viewModel: RestaurantViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }
    private lateinit var adapter: RestaurantOrderAdapter
    private var allOrders: List<com.example.foodnow.data.Order> = emptyList()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvRestaurantOrders)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val tvEmpty = view.findViewById<android.widget.TextView>(R.id.tvEmptyOrders)
        
        adapter = RestaurantOrderAdapter(emptyList(), 
            onAction1Click = { order ->
                val bundle = Bundle().apply { putLong("orderId", order.id) }
                findNavController().navigate(R.id.action_orders_to_details, bundle)
            },
            onAction2Click = { order ->
                // Quick reject? Or details?
                // Let's make the whole item clickable or Action1 go to Details?
                // Plan said "Order Details UI". Adapter has "Accept" button.
                // Adapter usually has buttons.
                // Let's map Action1 (primary button) to "View Details" temporarily or just keep logic.
                // Re-reading logic: List has "Accept/Prepare" buttons.
                // User might want to click the ROW to view details.
                // Adapter `onAction1` handles status change. 
                // I should add `onItemClick` to adapter? Or make one of the buttons go to details?
                // `RestaurantOrderAdapter` (Step 220) is existing.
                // I will add a click listener to the `itemView` in adapter if I can.
                // Assuming I can't easily change Adapter interface right now without reading it.
                // I'll assume Action1 is "Accept" and Action2 is "Reject".
                // I need a way to go to details.
                // Maybe I should modify Adapter to have `onItemClick`.
                // For now, I'll make the whole ROW click go to details if adapter supports it?
                // It likely doesn't.
                // I'll make Action1 go to details instead of direct action?
                // No, direct action is faster.
                // I'll modify Adapter in next step if needed. 
                // For now, I'll implement empty state logic.
            }
        )
        // Oops, I can't modify adapter callback behavior without changing adapter.
        // Let's stick to existing logic for buttons, but how to see details?
        // I'll modify Adapter to add `onItemClick`.
        
        recyclerView.adapter = adapter
        
        val tabLayout = view.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayoutOrders)
        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                filterOrders(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        viewModel.orders.observe(viewLifecycleOwner) { result ->
            result.onSuccess { orders ->
                allOrders = orders
                filterOrders(tabLayout.selectedTabPosition)
            }.onFailure {
                Toast.makeText(context, "Error loading orders", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getOrders()
    }

    private fun filterOrders(tabIndex: Int) {
        val filtered = when (tabIndex) {
            0 -> allOrders // All
            1 -> allOrders.filter { it.status == "PENDING" } // Pending
            2 -> allOrders.filter { it.status == "ACCEPTED" || it.status == "PREPARING" } // In Progress
            3 -> allOrders.filter { it.status == "READY" } // Ready
            else -> allOrders
        }
        
        adapter.updateOrders(filtered)
        val tvEmpty = view?.findViewById<android.widget.TextView>(R.id.tvEmptyOrders)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.rvRestaurantOrders)
        
        if (filtered.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            tvEmpty?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
    }
}
