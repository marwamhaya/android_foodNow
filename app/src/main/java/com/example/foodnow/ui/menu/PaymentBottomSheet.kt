package com.example.foodnow.ui.menu

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.foodnow.R
import com.example.foodnow.databinding.BottomSheetPaymentBinding
import com.example.foodnow.utils.CartManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class PaymentBottomSheet(private val viewModel: MenuViewModel, private val restaurantId: Long) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPaymentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalAmount = BigDecimal.valueOf(CartManager.getTotal())
        binding.tvPaymentAmount.text = "Amount to Pay: ${String.format("%.2f", totalAmount)} DH"

        binding.btnConfirmPayment.setOnClickListener {
            val selectedId = binding.rgPaymentMethods.checkedRadioButtonId
            val method = if (selectedId == R.id.rbCard) "CARD_SIMULATION" else "CASH_ON_DELIVERY"
            
            // Disable button
            binding.btnConfirmPayment.isEnabled = false
            binding.btnConfirmPayment.text = "Processing..."
            
            viewModel.processPayment(totalAmount, method)
        }

        viewModel.paymentResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Payment Successful: ${it.message}", Toast.LENGTH_SHORT).show()
                // Get GPS location and place order
                placeOrderWithLocation()
            }.onFailure {
                binding.btnConfirmPayment.isEnabled = true
                binding.btnConfirmPayment.text = "Pay & Order"
                Toast.makeText(context, "Payment Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.orderResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
                dismiss() // This dismisses Payment Sheet
                // We should also ensure Cart Sheet is dismissed. 
                // However, since CartSheet opened this, usually we use a listener or shared viewmodel.
                // Assuming CartSheet observes cart empty -> dismisses itself.
            }.onFailure {
               // Order failed but payment succeeded? Complex rollback scenario usually. 
               // For demo/sim, we just show error.
               Toast.makeText(context, "Order Creation Failed: ${it.message}", Toast.LENGTH_SHORT).show()
               dismiss()
            }
        }
    }
    
    private fun placeOrderWithLocation() {
        try {
            // Check location permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                // No permission - use default coordinates (0,0) or show error
                Toast.makeText(context, "Location permission not granted. Using default location.", Toast.LENGTH_SHORT).show()
                viewModel.placeOrder(restaurantId, 0.0, 0.0)
                return
            }
            
            // Get location
            val locationManager = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            if (location != null) {
                viewModel.placeOrder(restaurantId, location.latitude, location.longitude)
            } else {
                // No location available - use default
                Toast.makeText(context, "Unable to get current location. Using default.", Toast.LENGTH_SHORT).show()
                viewModel.placeOrder(restaurantId, 0.0, 0.0)
            }
        } catch (e: Exception) {
            android.util.Log.e("PaymentBottomSheet", "Error getting location", e)
            // Fallback to default location
            viewModel.placeOrder(restaurantId, 0.0, 0.0)
        }
    }
}
