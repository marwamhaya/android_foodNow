package com.example.foodnow.ui.restaurant

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory

class MenuItemDetailsFragment : Fragment(R.layout.fragment_menu_item_details) {

    private val viewModel: RestaurantViewModel by activityViewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    private var menuItemId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuItemId = arguments?.getLong("menuItemId", -1L) ?: -1L
        if (menuItemId == -1L) {
            findNavController().popBackStack()
            return
        }

        val ivImage = view.findViewById<ImageView>(R.id.ivMenuItemImage)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvAvailability = view.findViewById<TextView>(R.id.tvAvailability)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvSupplementsHeader = view.findViewById<TextView>(R.id.tvSupplementsHeader)
        val rvOptionGroups = view.findViewById<RecyclerView>(R.id.rvOptionGroups)
        val tvNoSupplements = view.findViewById<TextView>(R.id.tvNoSupplements)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)

        rvOptionGroups.layoutManager = LinearLayoutManager(context)

        // Observe selected menu item
        viewModel.selectedMenuItem.observe(viewLifecycleOwner) { item ->
            if (item == null) return@observe

            // Image
            if (!item.imageUrl.isNullOrEmpty()) {
                val fullUrl = if (item.imageUrl.startsWith("http")) item.imageUrl
                else "http://100.79.107.106:8080${item.imageUrl}"
                Glide.with(this).load(fullUrl).into(ivImage)
            }

            // Name
            tvName.text = item.name

            // Availability badge
            if (item.isAvailable) {
                tvAvailability.text = "🟢 Available"
                tvAvailability.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
            } else {
                tvAvailability.text = "🔴 Not Available"
                tvAvailability.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            }

            // Category
            tvCategory.text = "📂 ${item.category ?: "Uncategorized"}"

            // Price
            tvPrice.text = "${item.price} DH"

            // Description
            tvDescription.text = item.description ?: "No description available"

            // Option Groups
            val groups = item.optionGroups
            if (groups.isNotEmpty()) {
                tvNoSupplements.visibility = View.GONE
                rvOptionGroups.visibility = View.VISIBLE
                rvOptionGroups.adapter = OptionGroupAdapter { /* Read-only, no action */ }
                (rvOptionGroups.adapter as OptionGroupAdapter).submitList(groups)
            } else {
                tvNoSupplements.visibility = View.VISIBLE
                rvOptionGroups.visibility = View.GONE
            }
        }

        // Load item (fallback to API if not in cache)
        viewModel.loadMenuItemById(menuItemId)

        // Edit button
        btnEdit.setOnClickListener {
            val bundle = Bundle().apply { putLong("menuItemId", menuItemId) }
            findNavController().navigate(R.id.action_details_to_edit, bundle)
        }
    }
}
