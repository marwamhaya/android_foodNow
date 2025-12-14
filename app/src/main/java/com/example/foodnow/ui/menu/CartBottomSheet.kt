package com.example.foodnow.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.R
import com.example.foodnow.databinding.BottomSheetCartBinding
import com.example.foodnow.utils.CartItem
import com.example.foodnow.utils.CartManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class CartBottomSheet(private val viewModel: MenuViewModel, private val restaurantId: Long) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCartBinding
    private lateinit var adapter: CartAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CartAdapter { idx, newQty ->
            CartManager.updateQuantity(idx, newQty)
        }
        
        binding.rvCartItems.layoutManager = LinearLayoutManager(context)
        binding.rvCartItems.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            CartManager.cartItems.collect { items ->
                adapter.submitList(items)
                updateTotal(items)
                if (items.isEmpty()) {
                     dismiss()
                }
            }
        }

        binding.btnPlaceOrder.setOnClickListener {
            // viewModel.placeOrder(restaurantId) // Old logic
            // Open Payment Sheet
            val paymentSheet = PaymentBottomSheet(viewModel, restaurantId)
            paymentSheet.show(parentFragmentManager, "PaymentBottomSheet")
            // Optional: Dismiss cart sheet if you want a clean transition, 
            // but keeping it might be okay if Payment is just a confirm. 
            // For now, let's keep it open or dismiss? Dismissing is safer UI-wise.
            dismiss()
        }
    }

    private fun updateTotal(items: List<CartItem>) {
        val total = CartManager.getTotal()
        binding.tvTotal.text = "Total: ${String.format("%.2f", total)} DH"
        binding.btnPlaceOrder.text = "Place Order - ${String.format("%.2f", total)} DH"
    }
}

class CartAdapter(private val onQuantityChange: (Int, Int) -> Unit) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    private var items = listOf<CartItem>()

    fun submitList(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        
        holder.tvName.text = item.menuItem.name
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvPrice.text = "${String.format("%.2f", item.totalPrice)} DH"
        
        // Display selected options/supplements
        if (item.selectedOptionIds.isNotEmpty()) {
            val optionNames = mutableListOf<String>()
            item.menuItem.optionGroups?.forEach { group ->
                group.options?.forEach { option ->
                    if (item.selectedOptionIds.contains(option.id)) {
                        optionNames.add(option.name)
                    }
                }
            }
            if (optionNames.isNotEmpty()) {
                holder.tvOptions.visibility = View.VISIBLE
                holder.tvOptions.text = "• ${optionNames.joinToString(", ")}"
            } else {
                holder.tvOptions.visibility = View.GONE
            }
        } else {
            holder.tvOptions.visibility = View.GONE
        }
        
        // Quantity controls
        holder.btnDecrease.setOnClickListener {
            val newQty = (item.quantity - 1).coerceAtLeast(0)
            onQuantityChange(position, newQty)
        }
        
        holder.btnIncrease.setOnClickListener {
            onQuantityChange(position, item.quantity + 1)
        }
    }

    override fun getItemCount() = items.size

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCartItemName)
        val tvOptions: TextView = itemView.findViewById(R.id.tvCartItemOptions)
        val tvPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvCartItemQuantity)
        val btnDecrease: TextView = itemView.findViewById(R.id.btnDecreaseQty)
        val btnIncrease: TextView = itemView.findViewById(R.id.btnIncreaseQty)
    }
}

