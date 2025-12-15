package com.example.foodnow.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tvTotalUsers = view.findViewById<TextView>(R.id.tvTotalUsers)
        val tvTotalOrders = view.findViewById<TextView>(R.id.tvTotalOrders)
        val tvTotalRestaurants = view.findViewById<TextView>(R.id.tvTotalRestaurants)
        val tvNewUsers = view.findViewById<TextView>(R.id.tvNewUsers)
        val tvDeliveryPerformance = view.findViewById<TextView>(R.id.tvDeliveryPerformance)
        val pbDeliveryPerformance = view.findViewById<android.widget.ProgressBar>(R.id.pbDeliveryPerformance)
        
        CoroutineScope(Dispatchers.Main).launch {
             try {
                 val repo = (requireActivity().application as FoodNowApp).repository
                 val response = withContext(Dispatchers.IO) { repo.getSystemStats() }
                 if (response.isSuccessful && response.body() != null) {
                     val stats = response.body()!!
                     
                     tvTotalUsers.text = (stats["totalUsers"] as? Double)?.toInt()?.toString() ?: (stats["totalUsers"] as? Int)?.toString() ?: "0"
                     tvTotalOrders.text = (stats["totalOrders"] as? Double)?.toInt()?.toString() ?: (stats["totalOrders"] as? Int)?.toString() ?: "0"
                     tvTotalRestaurants.text = (stats["totalRestaurants"] as? Double)?.toInt()?.toString() ?: (stats["totalRestaurants"] as? Int)?.toString() ?: "0"
                     tvNewUsers.text = (stats["newUsersCount"] as? Double)?.toInt()?.toString() ?: (stats["newUsersCount"] as? Int)?.toString() ?: "0"
                     
                     val performance = (stats["deliveryPerformance"] as? Double) ?: 0.0
                     tvDeliveryPerformance.text = "$performance%"
                     pbDeliveryPerformance.progress = performance.toInt()
                 }
             } catch (e: Exception) {
                 // Handle error
             }
        }
    }
}
