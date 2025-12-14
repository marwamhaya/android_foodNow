package com.example.foodnow.ui.menu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.databinding.FragmentMenuBinding
import androidx.lifecycle.lifecycleScope
import com.example.foodnow.ui.ViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class MenuFragment : Fragment(R.layout.fragment_menu) {

    private lateinit var binding: FragmentMenuBinding
    private lateinit var adapter: MenuAdapter
    
    private val viewModel: MenuViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMenuBinding.bind(view)

        val restaurantId = arguments?.getLong("restaurantId") ?: -1L
        if (restaurantId == -1L) {
            Toast.makeText(context, "Invalid Restaurant ID", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = MenuAdapter(emptyList()) { item ->
            // Open Details Bottom Sheet
            val bottomSheet = ItemDetailsBottomSheet(item, restaurantId)
            bottomSheet.show(parentFragmentManager, "ItemDetailsBottomSheet")
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadMenu(restaurantId)

        viewModel.menuItems.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            result.onSuccess { list ->
                adapter.updateData(list)
            }.onFailure {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observe CartManager instead of ViewModel.cart
        viewLifecycleOwner.lifecycleScope.launch {
            com.example.foodnow.utils.CartManager.cartItems.collect { cartItems ->
                 if (cartItems.isNotEmpty() && com.example.foodnow.utils.CartManager.getCurrentRestaurantId() == restaurantId) {
                    binding.layoutCart.visibility = View.VISIBLE
                    val total = com.example.foodnow.utils.CartManager.getTotal()
                    binding.btnPlaceOrder.text = "View Cart (${cartItems.sumOf { it.quantity }}) - ${String.format("%.2f", total)}€"
                    binding.btnPlaceOrder.setOnClickListener {
                        val cartSheet = CartBottomSheet(viewModel, restaurantId)
                        cartSheet.show(parentFragmentManager, "CartBottomSheet")
                    }
                } else {
                    binding.layoutCart.visibility = View.GONE
                }
            }
        }

        viewModel.orderResult.observe(viewLifecycleOwner) { result ->
             result.onSuccess {
                 Toast.makeText(context, "Order Placed! ID: ${it.id}", Toast.LENGTH_LONG).show()
                 // Navigate to Orders or Track
                 // findNavController().navigate(R.id.action_menu_to_orders) // If action exists
                 requireActivity().onBackPressedDispatcher.onBackPressed() // Go back for now
             }.onFailure {
                 Toast.makeText(context, "Order Failed: ${it.message}", Toast.LENGTH_LONG).show()
             }
        }
    }
}
