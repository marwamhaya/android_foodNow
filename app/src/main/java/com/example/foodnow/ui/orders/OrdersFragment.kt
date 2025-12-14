package com.example.foodnow.ui.orders

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.databinding.FragmentOrdersBinding
import com.example.foodnow.ui.ViewModelFactory

class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private lateinit var binding: FragmentOrdersBinding
    private lateinit var adapter: OrdersAdapter
    
    private val viewModel: OrdersViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOrdersBinding.bind(view)

        adapter = OrdersAdapter(emptyList()) { order ->
            // Show order details
            val detailsSheet = OrderDetailsBottomSheet(order)
            detailsSheet.show(parentFragmentManager, "OrderDetailsBottomSheet")
        }
        
        binding.rvOrders.layoutManager = LinearLayoutManager(context)
        binding.rvOrders.adapter = adapter

        viewModel.orders.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            result.onSuccess { list ->
                if (list.isEmpty()) {
                    binding.rvOrders.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                } else {
                    binding.rvOrders.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    adapter.updateData(list)
                }
            }.onFailure {
                binding.rvOrders.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.text = it.message
                binding.tvError.visibility = View.VISIBLE
            }
        }

        binding.progressBar.visibility = View.VISIBLE
        viewModel.fetchOrders()
    }
}
