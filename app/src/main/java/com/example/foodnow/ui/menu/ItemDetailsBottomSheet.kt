package com.example.foodnow.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.example.foodnow.R
import com.example.foodnow.data.MenuItemResponse
import com.example.foodnow.data.MenuOptionResponse
import com.example.foodnow.databinding.BottomSheetItemDetailsBinding
import com.example.foodnow.utils.CartManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class ItemDetailsBottomSheet(
    private val menuItem: MenuItemResponse,
    private val restaurantId: Long
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetItemDetailsBinding
    private var quantity = 1
    private val selectedOptions = mutableMapOf<Long, MutableList<MenuOptionResponse>>() // GroupId -> List<Option>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetItemDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvItemName.text = menuItem.name
        binding.tvItemDesc.text = menuItem.description
        binding.tvCategory.text = "📂 ${menuItem.category}"
        
        // Display availability status
        if (menuItem.isAvailable == true) {
            binding.tvAvailability.text = "✅ Available"
            binding.tvAvailability.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        } else {
            binding.tvAvailability.text = "❌ Sold Out"
            binding.tvAvailability.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            binding.btnAddToCart.isEnabled = false
            binding.btnAddToCart.alpha = 0.5f
        }
        
        updateTotalPrice()

        // Dynamically add options
        menuItem.optionGroups.orEmpty().forEach { group ->
            val groupTitle = TextView(context).apply {
                text = "${group.name} ${if (group.isRequired) "(Required)" else "(Optional)"}"
                textSize = 16f
                setPadding(0, 16, 0, 8)
            }
            binding.optionsContainer.addView(groupTitle)

            if (group.isMultiple) {
                // Checkboxes
                group.options.forEach { option ->
                    val cb = CheckBox(context).apply {
                        text = "${option.name} (+${option.extraPrice}€)"
                        setOnCheckedChangeListener { _, isChecked ->
                            toggleOption(group.id, option, isChecked, true)
                        }
                    }
                    binding.optionsContainer.addView(cb)
                }
            } else {
                // RadioGroup
                val radioGroup = RadioGroup(context)
                group.options.forEach { option ->
                    val rb = RadioButton(context).apply {
                        text = "${option.name} (+${option.extraPrice}€)"
                        tag = option
                    }
                    radioGroup.addView(rb)
                }
                radioGroup.setOnCheckedChangeListener { groupView, checkedId ->
                    val rb = groupView.findViewById<RadioButton>(checkedId)
                    val option = rb.tag as MenuOptionResponse
                    // Clear previous selection for this group
                    selectedOptions[group.id] = mutableListOf(option)
                    updateTotalPrice()
                }
                binding.optionsContainer.addView(radioGroup)
            }
        }

        binding.btnIncrease.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
            updateTotalPrice()
        }

        binding.btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
                updateTotalPrice()
            }
        }

        binding.btnAddToCart.setOnClickListener {
            if (validateSelections()) {
                val flatOptions = selectedOptions.values.flatten()
                val optionIds = flatOptions.map { it.id }
                val optionsPrice = flatOptions.sumOf { it.extraPrice.toDouble() }

                val success = CartManager.addItem(menuItem, quantity, optionIds, optionsPrice, restaurantId)
                if (success) {
                    dismiss()
                    Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                } else {
                    // Show mismatch dialog
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Start new basket?")
                        .setMessage("Your basket contains items from another restaurant. Do you want to clear it and add this item?")
                        .setPositiveButton("New Basket") { _, _ ->
                            CartManager.clearCart()
                            CartManager.addItem(menuItem, quantity, optionIds, optionsPrice, restaurantId)
                            dismiss()
                            Toast.makeText(context, "Cart cleared and item added", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }

    private fun toggleOption(groupId: Long, option: MenuOptionResponse, isSelected: Boolean, isMultiple: Boolean) {
        if (!selectedOptions.containsKey(groupId)) {
            selectedOptions[groupId] = mutableListOf()
        }
        val list = selectedOptions[groupId]!!
        if (isSelected) {
            list.add(option)
        } else {
            list.remove(option)
        }
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val basePrice = menuItem.price.toDouble()
        val optionsPrice = selectedOptions.values.flatten().sumOf { it.extraPrice.toDouble() }
        val total = (basePrice + optionsPrice) * quantity
        binding.btnAddToCart.text = "Add to Cart - ${String.format("%.2f", total)}€"
    }

    private fun validateSelections(): Boolean {
        menuItem.optionGroups.orEmpty().filter { it.isRequired }.forEach { group ->
            if (selectedOptions[group.id].isNullOrEmpty()) {
                Toast.makeText(context, "Please select ${group.name}", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }
}
