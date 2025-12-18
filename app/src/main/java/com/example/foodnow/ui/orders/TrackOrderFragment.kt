package com.example.foodnow.ui.orders

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.data.DeliveryResponse
import com.example.foodnow.ui.ViewModelFactory
import com.example.foodnow.ui.livreur.LivreurViewModel
import com.example.foodnow.utils.MarkerAnimator
import com.example.foodnow.utils.NavigationHelper
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class TrackOrderFragment : Fragment(R.layout.fragment_track_order) {

    private lateinit var mapView: MapView
    private lateinit var tvDriverInfo: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvETA: TextView
    private lateinit var tvConnectionStatus: TextView
    private lateinit var cardConnectionStatus: CardView
    private lateinit var timelineStep1: View
    private lateinit var timelineStep2: View
    private lateinit var timelineStep3: View
    private lateinit var timelineStep4: View
    private lateinit var timelineStep5: View
    
    // UI Fallback
    private lateinit var tvTrackingUnavailable: TextView
    
    private var stompClient: StompClient? = null
    private val gson = Gson()
    private var driverMarker: Marker? = null
    private var clientMarker: Marker? = null
    private var routeLine: Polyline? = null
    private var clientLocation: GeoPoint? = null
    private var lastDriverLocation: GeoPoint? = null
    private var updateJob: Job? = null
    private var reconnectAttempts = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_LONG).show()
        }
    }
    
    private val viewModel: LivreurViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
        private const val TAG = "TrackOrder"
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_DELAY = 3000L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        initializeMap()
        
        // Check permissions
        checkAndRequestLocationPermission()
        
        // Fetch delivery details to get client address
        val orderId = arguments?.getLong("orderId") ?: 1L
        fetchDeliveryDetails(orderId)
        
        connectStomp()
        
        // Start periodic UI updates
        startPeriodicUpdates()
    }
    
    private fun initializeViews(view: View) {
        mapView = view.findViewById(R.id.mapView)
        tvDriverInfo = view.findViewById(R.id.tvDriverInfo)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvETA = view.findViewById(R.id.tvETA)
        tvConnectionStatus = view.findViewById(R.id.tvConnectionStatus)
        cardConnectionStatus = view.findViewById(R.id.cardConnectionStatus)
        timelineStep1 = view.findViewById(R.id.timelineStep1)
        timelineStep2 = view.findViewById(R.id.timelineStep2)
        timelineStep3 = view.findViewById(R.id.timelineStep3)
        timelineStep4 = view.findViewById(R.id.timelineStep4)
        timelineStep5 = view.findViewById(R.id.timelineStep5)
        tvTrackingUnavailable = view.findViewById(R.id.tvTrackingUnavailable)
    }
    
    private fun initializeMap() {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(33.5731, -7.5898))
    }

    private fun connectStomp() {
        val tokenManager = com.example.foodnow.data.TokenManager(requireContext())
        val token = tokenManager.getToken() ?: return
        val orderId = arguments?.getLong("orderId") ?: 1L

        com.example.foodnow.service.WebSocketService.connect(token)
        
        // Subscribe to transient driver location: /topic/order/{orderId}/driver-location
        com.example.foodnow.service.WebSocketService.subscribeToTopic("/topic/order/$orderId/driver-location", token) { payload ->
            try {
                val location = gson.fromJson(payload, com.example.foodnow.data.LocationUpdateDto::class.java)
                requireActivity().runOnUiThread {
                    updateDriverLocation(location)
                    showConnectionStatus("Live", "#4CAF50")
                    tvTrackingUnavailable.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Parsing error", e)
            }
        }

        // Subscribe to status updates
        com.example.foodnow.service.WebSocketService.subscribeToTopic("/topic/delivery/$orderId/status", token) { payload ->
            requireActivity().runOnUiThread {
                updateDeliveryTimeline(payload)
            }
        }
    }
    
    private fun showConnectionStatus(message: String, color: String) {
        cardConnectionStatus.visibility = View.VISIBLE
        tvConnectionStatus.text = message
        tvConnectionStatus.setBackgroundColor(Color.parseColor(color))
    }
    
    private fun handleConnectionLoss() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++
            Toast.makeText(context, "Connection lost. Reconnecting... (${reconnectAttempts}/$MAX_RECONNECT_ATTEMPTS)", Toast.LENGTH_SHORT).show()
            
            CoroutineScope(Dispatchers.IO).launch {
                delay(RECONNECT_DELAY)
                withContext(Dispatchers.Main) {
                    try {
                        stompClient?.disconnect()
                        connectStomp()
                    } catch (e: Exception) {
                        Log.e(TAG, "Reconnection failed", e)
                    }
                }
            }
        } else {
            Toast.makeText(context, "Unable to connect to server. Please check your connection.", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }
    
    
    private fun fetchDeliveryDetails(orderId: Long) {
        viewModel.getAssignedDeliveries()
        viewModel.deliveries.observe(viewLifecycleOwner) { result ->
            result.onSuccess { deliveries ->
                val delivery = deliveries.find { it.orderId == orderId }
                delivery?.let { del ->
                    // Centering logic: Center on restaurant first
                    if (del.restaurantLatitude != null && del.restaurantLongitude != null) {
                        val restaurantLoc = GeoPoint(del.restaurantLatitude, del.restaurantLongitude)
                        mapView.controller.setCenter(restaurantLoc)
                        addRestaurantMarker(restaurantLoc, del.restaurantName)
                    }

                    if (clientLocation == null && del.clientLatitude != null && del.clientLongitude != null) {
                        val geoPoint = GeoPoint(del.clientLatitude, del.clientLongitude)
                        addClientMarkerAt(geoPoint, del.clientAddress)
                    }
                    tvDriverInfo.text = "Commande de: ${del.clientName}\n${del.clientAddress}"
                    updateDeliveryTimeline(del.status)
                }
            }
        }
    }

    private fun addRestaurantMarker(loc: GeoPoint, name: String) {
         val marker = Marker(mapView)
         marker.position = loc
         marker.title = name
         marker.icon = resources.getDrawable(R.drawable.ic_restaurant_24, null)
         mapView.overlays.add(marker)
         mapView.invalidate()
    }
    
    private fun addClientMarkerAt(geoPoint: GeoPoint, address: String) {
        clientLocation = geoPoint
        
        if (clientMarker == null) {
            clientMarker = Marker(mapView)
            clientMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            clientMarker?.icon = resources.getDrawable(android.R.drawable.ic_dialog_map, null)
            mapView.overlays.add(clientMarker)
        }
        
        clientMarker?.position = geoPoint
        clientMarker?.title = "Delivery Location"
        clientMarker?.snippet = address
        
        // Center on client location initially
        mapView.controller.setCenter(geoPoint)
        mapView.invalidate()
        
        Log.d(TAG, "Client marker added at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }

    private fun updateDriverLocation(location: com.example.foodnow.data.LocationUpdateDto) {
        val newPosition = GeoPoint(location.latitude, location.longitude)
        
        if (driverMarker == null) {
            driverMarker = Marker(mapView)
            driverMarker?.title = "Livreur"
            driverMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            driverMarker?.icon = resources.getDrawable(R.drawable.ic_delivery_dining_24, null)
            mapView.overlays.add(driverMarker)
            driverMarker?.position = newPosition
            
            // First time we get location, hide "Connecting" if it was orange
            cardConnectionStatus.visibility = View.GONE
        } else {
            driverMarker?.let { marker ->
                MarkerAnimator.animateMarkerToPosition(marker, newPosition, mapView, 2000L)
            }
        }
        
        lastDriverLocation = newPosition
        updateRouteLine(newPosition)
        updateDistanceAndETA(newPosition)
        
        // Zoom to fit both if it's the first time or they are far apart
        if (reconnectAttempts == 0) {
            zoomToBoundingBox()
        }
        
        Log.d(TAG, "Driver position updated: ${location.latitude}, ${location.longitude}")
    }

    private fun zoomToBoundingBox() {
        val driverPos = lastDriverLocation ?: return
        val clientPos = clientLocation ?: return
        val points = arrayListOf(driverPos, clientPos)
        val box = org.osmdroid.util.BoundingBox.fromGeoPoints(points)
        mapView.zoomToBoundingBox(box.increaseByScale(1.4f), true, 100)
    }
    
    private fun updateRouteLine(driverPosition: GeoPoint) {
        val clientGeo = clientLocation ?: return
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val roadManager = OSRMRoadManager(requireContext(), "FoodNowApp/1.0")
                val road = roadManager.getRoad(arrayListOf(driverPosition, clientGeo))
                
                withContext(Dispatchers.Main) {
                    if (road.mStatus == org.osmdroid.bonuspack.routing.Road.STATUS_OK) {
                        routeLine?.let { mapView.overlays.remove(it) }
                        routeLine = RoadManager.buildRoadOverlay(road)
                        routeLine?.outlinePaint?.color = Color.parseColor("#007AFF")
                        routeLine?.outlinePaint?.strokeWidth = 12f
                        mapView.overlays.add(0, routeLine)
                        
                        updateDistanceAndETAFromRoad(road)
                        mapView.invalidate()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Route update error", e)
                // Fallback to straight line on error
                withContext(Dispatchers.Main) {
                    drawStraightLine(driverPosition, clientGeo)
                }
            }
        }
    }

    private fun drawStraightLine(start: GeoPoint, end: GeoPoint) {
        if (routeLine == null) {
            routeLine = Polyline()
            routeLine?.outlinePaint?.color = Color.BLUE
            routeLine?.outlinePaint?.strokeWidth = 8f
            mapView.overlays.add(0, routeLine)
        }
        routeLine?.setPoints(listOf(start, end))
        mapView.invalidate()
    }
    
    private fun updateDistanceAndETAFromRoad(road: org.osmdroid.bonuspack.routing.Road) {
        val distanceKm = road.mLength
        val durationMin = (road.mDuration / 60.0).toInt()
        
        tvDistance.text = NavigationHelper.formatDistance(distanceKm)
        tvETA.text = NavigationHelper.formatETA(durationMin)
        
        if (distanceKm < 0.5) {
            tvDriverInfo.text = "Le livreur arrive !"
        } else {
            tvDriverInfo.text = "Livreur en route vers vous (${NavigationHelper.formatDistance(distanceKm)})"
        }
    }
    
    private fun updateDistanceAndETA(driverPosition: GeoPoint) {
        // This is now handled by updateDistanceAndETAFromRoad within updateRouteLine
        // but keeping it as fallback for initial calculation
        if (tvDistance.text.isEmpty()) {
            val dist = NavigationHelper.calculateDistance(driverPosition, clientLocation ?: return)
            tvDistance.text = NavigationHelper.formatDistance(dist)
            tvETA.text = NavigationHelper.formatETA(NavigationHelper.calculateETA(dist))
        }
    }
    
    private fun updateDeliveryTimeline(status: String) {
        val activeColor = Color.parseColor("#4CAF50")
        val inactiveColor = Color.parseColor("#E0E0E0")
        
        // Reset steps
        listOf(timelineStep1, timelineStep2, timelineStep3, timelineStep4, timelineStep5).forEach { 
            it?.setBackgroundColor(inactiveColor) 
        }
        
        when (status.uppercase()) {
            "CREATED", "PENDING" -> {
                timelineStep1.setBackgroundColor(activeColor)
                tvDriverInfo.text = "Commande reçue"
            }
            "ACCEPTED", "RESTAURANT_ACCEPTED" -> {
                timelineStep1.setBackgroundColor(activeColor)
                timelineStep2.setBackgroundColor(activeColor)
                tvDriverInfo.text = "Restaurant prépare votre commande"
            }
            "DRIVER_ASSIGNED", "READY_FOR_PICKUP", "PICKED_UP", "DELIVERY_ACCEPTED" -> {
                timelineStep1.setBackgroundColor(activeColor)
                timelineStep2.setBackgroundColor(activeColor)
                timelineStep3.setBackgroundColor(activeColor)
                tvDriverInfo.text = "Livreur en route vers le restaurant"
            }
            "ON_THE_WAY", "IN_DELIVERY" -> {
                timelineStep1.setBackgroundColor(activeColor)
                timelineStep2.setBackgroundColor(activeColor)
                timelineStep3.setBackgroundColor(activeColor)
                timelineStep4.setBackgroundColor(activeColor)
                tvDriverInfo.text = "Livreur en route vers vous !"
            }
            "DELIVERED", "COMPLETED" -> {
                listOf(timelineStep1, timelineStep2, timelineStep3, timelineStep4, timelineStep5).forEach { 
                    it?.setBackgroundColor(activeColor) 
                }
                tvDriverInfo.text = "Commande livrée. Bon appétit !"
                tvTrackingUnavailable.visibility = View.GONE
            }
        }
    }
    
    private fun startPeriodicUpdates() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                // Periodically adjust map view to show both markers
                lastDriverLocation?.let { driverPos ->
                    clientLocation?.let { clientPos ->
                        val points = listOf(driverPos, clientPos)
                        val boundingBox = BoundingBox.fromGeoPoints(points)
                        mapView.post {
                            mapView.zoomToBoundingBox(boundingBox, true, 150)
                        }
                    }
                }
                delay(10000L) // Every 10 seconds
            }
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

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        mapView.onDetach()
        try {
            stompClient?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect", e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
    
}
