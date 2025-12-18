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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.android.gms.location.*
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts

class PaymentBottomSheet(private val viewModel: MenuViewModel, private val restaurantId: Long) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPaymentBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentGPSLocation: android.location.Location? = null
    private val PERMISSION_REQUEST_CODE = 2001

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startLocationUpdates()
        } else {
            showBlockingPermissionDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetPaymentBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirmPayment.isEnabled = false
        binding.tvGpsStatus.text = "🔍 Recherche de votre position GPS..."
        
        checkLocationPermission()

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
    
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationUpdates()
        }
    }


    private fun showBlockingPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Requise")
            .setMessage("L'accès à votre position est obligatoire pour passer une commande et assurer une livraison précise.")
            .setCancelable(false)
            .setPositiveButton("Réessayer") { _, _ -> checkLocationPermission() }
            .setNegativeButton("Annuler") { _, _ -> dismiss() }
            .show()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdates(10) // Small burst to get fix
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    currentGPSLocation = location
                    binding.tvGpsStatus.text = "✅ Position GPS obtenue"
                    binding.tvGpsStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    binding.btnConfirmPayment.isEnabled = true
                }
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
    
    private fun placeOrderWithLocation() {
        val loc = currentGPSLocation
        if (loc != null) {
            viewModel.placeOrder(restaurantId, loc.latitude, loc.longitude)
        } else {
            Toast.makeText(context, "Erreur: GPS non disponible", Toast.LENGTH_LONG).show()
            binding.btnConfirmPayment.isEnabled = true
        }
    }
}
