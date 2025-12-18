package com.example.foodnow.ui.livreur

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

class LivreurDashboardFragment : Fragment(R.layout.fragment_livreur_dashboard) {

    private val viewModel: LivreurViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }
    private lateinit var adapter: DeliveryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDeliveries)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DeliveryAdapter(emptyList()) { delivery ->
            when (delivery.status) {
                "PENDING" -> viewModel.acceptDeliveryRequest(delivery.id)
                "DELIVERY_ACCEPTED", "READY_FOR_PICKUP" -> {
                    // Navigate to Map to start pickup process or just start it?
                    // Let's assume hitting "Pick Up" starts it, then it changes to PICKED_UP
                    viewModel.updateStatus(delivery.id, "PICKED_UP")
                    // Start service 
                    val intent = android.content.Intent(requireContext(), com.example.foodnow.service.LocationService::class.java)
                    intent.putExtra("order_id", delivery.orderId)
                    requireContext().startService(intent)
                    
                    // Then navigate to map immediately
                    val bundle = Bundle().apply { 
                        putLong("delivery_id", delivery.id)
                        putLong("orderId", delivery.orderId)
                    }
                    try {
                        findNavController().navigate(R.id.nav_active_delivery, bundle)
                    } catch (e: Exception) {
                        // Fallback manual transaction
                         parentFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_livreur, ActiveDeliveryFragment().apply { arguments = bundle })
                            .addToBackStack(null)
                            .commit()
                    }
                }
                "PICKED_UP", "IN_DELIVERY", "ON_THE_WAY" -> {
                    // Navigate to Map
                    val bundle = Bundle().apply { 
                        putLong("delivery_id", delivery.id)
                        putLong("orderId", delivery.orderId)
                    }
                    try {
                        findNavController().navigate(R.id.nav_active_delivery, bundle)
                    } catch (e: Exception) {
                         parentFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_livreur, ActiveDeliveryFragment().apply { arguments = bundle })
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        }
        recyclerView.adapter = adapter

        // Observer for Assigned Deliveries
        viewModel.deliveries.observe(viewLifecycleOwner) { result ->
            result.onSuccess { assignedList ->
                // Merge with available?
                // For simplicity, let's just show Assigned if any, then Available.
                // Or better: Use distinct livedata for UI list?
            }
        }
        
        // Observer for Available Requests
        viewModel.availableRequests.observe(viewLifecycleOwner) { result ->
             result.onSuccess { availableList ->
                 // Logic: Show Assigned + Available
                 // We need to store them locally or combine LiveData
                 val assigned = viewModel.deliveries.value?.getOrNull() ?: emptyList()
                 val merged = assigned + availableList
                 adapter.updateDeliveries(merged)
             }
        }
        
        // Use MediatorLiveData or manual merge?
        // Let's just trigger updates when either changes
        viewModel.deliveries.observe(viewLifecycleOwner) { 
             val assigned = it.getOrNull() ?: emptyList()
             val available = viewModel.availableRequests.value?.getOrNull() ?: emptyList()
             adapter.updateDeliveries(assigned + available)
        }

        viewModel.requestActionStatus.observe(viewLifecycleOwner) { result ->
             result.onSuccess { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
             result.onFailure { e -> Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
        }

        viewModel.getAssignedDeliveries()
        viewModel.getAvailableRequests()
        viewModel.startListeningForDeliveries()
        
        // Auto-refresh?
    }
}
