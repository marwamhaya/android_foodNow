package com.example.foodnow.ui.restaurant

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import java.math.BigDecimal

class OrderDetailsFragment : Fragment(R.layout.fragment_order_details) {

    private val viewModel: RestaurantViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    private var orderId: Long = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderId = arguments?.getLong("orderId", -1L) ?: -1L
        if (orderId == -1L) {
             return
        }
        
        // Ensure orders are loaded
        viewModel.getOrders()

        val tvId = view.findViewById<TextView>(R.id.tvOrderId)
        val tvDate = view.findViewById<TextView>(R.id.tvOrderDate)
        val tvStatus = view.findViewById<TextView>(R.id.tvOrderStatus)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalAmount)
        val rvItems = view.findViewById<RecyclerView>(R.id.rvOrderItems)
        val btnAction1 = view.findViewById<Button>(R.id.btnAction1)
        val btnAction2 = view.findViewById<Button>(R.id.btnAction2)

        rvItems.layoutManager = LinearLayoutManager(context)
        // Need simple item adapter. Reuse or create local?
        // I'll create a simple local logic or reuse OptionsAdapter if compatible? No.
        // I need to display OrderItem (name, quantity, price, options).
        // I'll assume we can create OrderItemsAdapter.

        viewModel.orders.observe(viewLifecycleOwner) { result ->
             val order = result.getOrNull()?.find { it.id == orderId }
             if (order != null) {
                 tvId.text = "Order #${order.id}"
                 tvDate.text = "Date: ${order.createdAt}"
                 tvStatus.text = order.status
                 tvTotal.text = "${String.format("%.2f", order.totalAmount)} DH"
                 
                 // Update buttons based on status
                 updateButtons(order.status, btnAction1, btnAction2)
                 
                 // Adapter
                 rvItems.adapter = OrderItemsAdapter(order.items)
             }
        }
        
        btnAction1.setOnClickListener {
             val order = viewModel.orders.value?.getOrNull()?.find { it.id == orderId } ?: return@setOnClickListener
             when(order.status) {
                 "PENDING" -> viewModel.acceptOrder(orderId)
                 "ACCEPTED" -> viewModel.prepareOrder(orderId)
                 "PREPARING" -> viewModel.readyOrder(orderId)
             }
        }
        
        btnAction2.setOnClickListener {
             val order = viewModel.orders.value?.getOrNull()?.find { it.id == orderId } ?: return@setOnClickListener
             if (order.status == "PENDING") {
                 viewModel.rejectOrder(orderId, "Busy")
             }
        }
    }

    private fun updateButtons(status: String, btn1: Button, btn2: Button) {
        when(status) {
            "PENDING" -> {
                btn1.text = "Accept Order"
                btn1.visibility = View.VISIBLE
                btn2.text = "Reject Order"
                btn2.visibility = View.VISIBLE
            }
            "ACCEPTED" -> {
                btn1.text = "Start Preparing"
                btn1.visibility = View.VISIBLE
                btn2.visibility = View.GONE
            }
            "PREPARING" -> {
                btn1.text = "Mark Ready"
                btn1.visibility = View.VISIBLE
                btn2.visibility = View.GONE
            }
            else -> {
                btn1.visibility = View.GONE
                btn2.visibility = View.GONE
            }
        }
    }
}
