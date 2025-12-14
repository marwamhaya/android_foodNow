package com.example.foodnow.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.databinding.FragmentHomeBinding
import com.example.foodnow.ui.ViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.foodnow.ui.menu.CartBottomSheet
import com.example.foodnow.ui.menu.MenuViewModel
import com.example.foodnow.utils.CartManager

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: RestaurantAdapter
    
    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    private val menuViewModel: MenuViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        adapter = RestaurantAdapter(emptyList()) { restaurantId ->
            val bundle = bundleOf("restaurantId" to restaurantId)
            findNavController().navigate(R.id.action_home_to_menu, bundle)
        }


        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE
        
        viewModel.restaurants.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            result.onSuccess { list ->
                if (list.isEmpty()) {
                    binding.tvError.text = "No restaurants found"
                    binding.tvError.visibility = View.VISIBLE
                } else {
                    binding.tvError.visibility = View.GONE
                    adapter.updateData(list)
                }
            }.onFailure {
                binding.tvError.text = "Error: ${it.message}"
                binding.tvError.visibility = View.VISIBLE
            }
        }

        // Observe CartManager to show global cart button
        viewLifecycleOwner.lifecycleScope.launch {
            CartManager.cartItems.collect { cartItems ->
                val restaurantId = CartManager.getCurrentRestaurantId()
                if (cartItems.isNotEmpty() && restaurantId != null) {
                    binding.layoutCart.visibility = View.VISIBLE
                    val total = CartManager.getTotal()
                    binding.btnPlaceOrder.text = "View Cart (${cartItems.sumOf { it.quantity }}) - ${String.format("%.2f", total)}€"
                    binding.btnPlaceOrder.setOnClickListener {
                        val cartSheet = CartBottomSheet(menuViewModel, restaurantId)
                        cartSheet.show(parentFragmentManager, "CartBottomSheet")
                    }
                } else {
                    binding.layoutCart.visibility = View.GONE
                }
            }
        }
    }
}
