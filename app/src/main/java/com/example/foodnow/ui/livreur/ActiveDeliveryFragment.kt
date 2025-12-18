package com.example.foodnow.ui.livreur

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.example.foodnow.utils.NavigationHelper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import com.example.foodnow.utils.MarkerAnimator
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.example.foodnow.data.DeliveryResponse
import com.example.foodnow.data.LocationUpdateDto

class ActiveDeliveryFragment : Fragment(R.layout.fragment_active_delivery) {

    private lateinit var mapView: MapView
    private lateinit var tvClientAddress: TextView
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvDeliveryStatus: TextView
    private lateinit var btnAction: Button
    private lateinit var cardSignalWarning: CardView

    private var clientMarker: Marker? = null
    private var driverMarker: Marker? = null
    private var roadOverlay: Polyline? = null
    
    private var clientLocation: GeoPoint? = null
    private var driverLocation: GeoPoint = GeoPoint(33.5731, -7.5898) 
    private var currentDelivery: DeliveryResponse? = null
    private var isFirstLocation = true
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    // Throttling variables
    private var lastLocationSentTime: Long = 0
    private var lastLocationSent: Location? = null
    private val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
    private val LOCATION_UPDATE_DISTANCE = 5f // 5 meters
    private val PERMISSION_REQUEST_CODE = 1001

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupLocationUpdates()
        } else {
            Toast.makeText(context, "Location permission is required for live tracking", Toast.LENGTH_LONG).show()
            tvDeliveryStatus.text = "Error: Permission GPS manquante"
            tvDeliveryStatus.setTextColor(Color.RED)
        }
    }

    private val viewModel: LivreurViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val deliveryId = arguments?.getLong("delivery_id") ?: return

        initializeViews(view)
        setupMap()
        checkAndRequestLocationPermission()
        observeDelivery(deliveryId)
    }

    private fun initializeViews(view: View) {
        mapView = view.findViewById(R.id.mapView)
        tvClientAddress = view.findViewById(R.id.tvClientAddress)
        tvOrderNumber = view.findViewById(R.id.tvOrderNumber)
        tvDeliveryStatus = view.findViewById(R.id.tvDeliveryStatus)
        btnAction = view.findViewById(R.id.btnDeliveryAction)
        cardSignalWarning = view.findViewById(R.id.cardSignalWarning)
    }

    private fun setupMap() {
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(driverLocation)
        
        driverMarker = Marker(mapView)
        driverMarker?.title = "Vous"
        driverMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        driverMarker?.icon = resources.getDrawable(android.R.drawable.ic_menu_mylocation, null)
        driverMarker?.position = driverLocation
        mapView.overlays.add(driverMarker)
    }

    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            setupLocationUpdates()
        }
    }


    private fun setupLocationUpdates() {
        if (!isAdded) return
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { handleNewLocation(it) }
                cardSignalWarning.visibility = View.GONE
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    cardSignalWarning.visibility = View.VISIBLE
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
            
            // Get initial last location for immediate display
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { handleNewLocation(it) }
            }
        }
    }

    private fun handleNewLocation(location: Location) {
        val newPoint = GeoPoint(location.latitude, location.longitude)
        driverLocation = newPoint
        
        if (driverMarker == null) {
            setupMap() // Ensure marker exists
        }
        
        driverMarker?.let { marker ->
            marker.position = newPoint
            if (isFirstLocation) {
                mapView.controller.animateTo(newPoint)
                isFirstLocation = false
            }
            mapView.invalidate()
        }
        
        currentDelivery?.let { del ->
            if (del.status == "ON_THE_WAY") {
                val currentTime = System.currentTimeMillis()
                val distanceMoved = lastLocationSent?.distanceTo(location) ?: Float.MAX_VALUE
                
                if (currentTime - lastLocationSentTime >= LOCATION_UPDATE_INTERVAL && distanceMoved >= 1.0f) {
                    val token = viewModel.getToken()
                    com.example.foodnow.service.WebSocketService.sendLocation(del.orderId, location.latitude, location.longitude, token)
                    lastLocationSentTime = currentTime
                    lastLocationSent = location
                    Log.d("ActiveDelivery", "Sent to server: ${location.latitude}, ${location.longitude}")
                }
            }
            
            // Update UI telemetry
            updateTelemetry(newPoint)
            
            // Recalculate route if needed
            if (System.currentTimeMillis() % 10 == 0L || isFirstLocation) {
                calculateRoute()
            }
        }
    }

    private fun updateTelemetry(driverPos: GeoPoint) {
        clientLocation?.let { clientPos ->
            val dist = NavigationHelper.calculateDistance(driverPos, clientPos)
            val eta = NavigationHelper.calculateETA(dist)
            
            val info = "Dist: ${NavigationHelper.formatDistance(dist)} • ETA: ${NavigationHelper.formatETA(eta)}"
            tvDeliveryStatus.text = "${formatStatus(currentDelivery?.status ?: "")} | $info"
        }
    }

    private fun observeDelivery(deliveryId: Long) {
        viewModel.deliveries.observe(viewLifecycleOwner) { result ->
            result.onSuccess { deliveries ->
                deliveries.find { it.id == deliveryId }?.let { del ->
                    currentDelivery = del
                    tvOrderNumber.text = "Commande #${del.orderId}"
                    tvDeliveryStatus.text = formatStatus(del.status)
                    tvClientAddress.text = del.clientAddress
                    
                    updateActionButton(del.status)

                    if (clientLocation == null && del.clientLatitude != null && del.clientLongitude != null) {
                        clientLocation = GeoPoint(del.clientLatitude, del.clientLongitude)
                        addClientMarker()
                        calculateRoute()
                    }
                }
            }
        }
        viewModel.getAssignedDeliveries()
    }

    private fun formatStatus(status: String): String {
        return when(status) {
            "DELIVERY_ACCEPTED" -> "Acceptée - En route vers le restaurant"
            "PICKED_UP" -> "Commande récupérée"
            "ON_THE_WAY" -> "En livraison vers le client"
            "DELIVERED" -> "Livrée"
            else -> status
        }
    }

    private fun updateActionButton(status: String) {
        btnAction.visibility = View.VISIBLE
        when (status) {
            "DELIVERY_ACCEPTED" -> {
                btnAction.text = "RÉCUPÉRER LA COMMANDE"
                btnAction.setOnClickListener { currentDelivery?.let { viewModel.updateStatus(it.id, "PICKED_UP") } }
            }
            "PICKED_UP" -> {
                btnAction.text = "DÉMARRER LA LIVRAISON"
                btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF"))
                btnAction.setOnClickListener { currentDelivery?.let { viewModel.updateStatus(it.id, "ON_THE_WAY") } }
            }
            "ON_THE_WAY" -> {
                btnAction.text = "MARQUER COMME LIVRÉE"
                btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                btnAction.setOnClickListener { currentDelivery?.let { viewModel.updateStatus(it.id, "DELIVERED") } }
            }
            "DELIVERED" -> {
                btnAction.visibility = View.GONE
            }
        }
    }

    private fun addClientMarker() {
        clientLocation?.let { loc ->
            if (clientMarker == null) {
                clientMarker = Marker(mapView)
                clientMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                clientMarker?.icon = resources.getDrawable(R.drawable.ic_restaurant_24, null)
                mapView.overlays.add(clientMarker)
            }
            clientMarker?.position = loc
            clientMarker?.title = "Client"
            mapView.invalidate()
        }
    }

    private fun calculateRoute() {
        val clientLoc = clientLocation ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val roadManager = OSRMRoadManager(requireContext(), "FoodNowApp/1.0")
                val road = roadManager.getRoad(arrayListOf(driverLocation, clientLoc))
                withContext(Dispatchers.Main) {
                    if (road.mStatus == org.osmdroid.bonuspack.routing.Road.STATUS_OK) {
                        roadOverlay?.let { mapView.overlays.remove(it) }
                        roadOverlay = RoadManager.buildRoadOverlay(road)
                        roadOverlay?.outlinePaint?.color = Color.parseColor("#007AFF")
                        roadOverlay?.outlinePaint?.strokeWidth = 14f
                        mapView.overlays.add(0, roadOverlay)
                        
                        if (isFirstLocation) {
                           zoomToBoundingBox()
                           isFirstLocation = false
                           // Initial center on driver for driver app
                           mapView.controller.animateTo(driverLocation)
                        }
                        mapView.invalidate()
                    }
                }
            } catch (e: Exception) {
                Log.e("ActiveDelivery", "Route error", e)
            }
        }
    }

    private fun zoomToBoundingBox() {
        clientLocation?.let { clientLoc ->
            val points = arrayListOf(driverLocation, clientLoc)
            val box = org.osmdroid.util.BoundingBox.fromGeoPoints(points)
            mapView.zoomToBoundingBox(box.increaseByScale(1.3f), true, 100)
        }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        scope.cancel()
        mapView.onDetach()
    }
}
