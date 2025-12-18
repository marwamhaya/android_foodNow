package com.example.foodnow.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import androidx.core.app.NotificationCompat
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private var activeOrderId: Long? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.hasExtra("order_id")) {
                activeOrderId = it.getLongExtra("order_id", -1)
                Log.d("LocationService", "Started tracking for order: $activeOrderId")
            }
        }
        val token = (application as FoodNowApp).repository.getToken()
        WebSocketService.connect(token)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        createNotificationChannel()
        startForeground(1, createNotification())

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "Location permission not granted")
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { sendLocationUpdate(it) }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
            Log.d("LocationService", "Fused location updates started")
        } catch (e: Exception) {
            Log.e("LocationService", "Error starting fused location updates", e)
        }
    }

    private fun sendLocationUpdate(location: Location) {
        scope.launch {
            try {
                Log.d("LocationService", "Location update: ${location.latitude}, ${location.longitude}")
                
                // Send to WebSocket if order is active
                activeOrderId?.let { orderId ->
                    if (orderId != -1L) {
                        val token = (application as FoodNowApp).repository.getToken()
                        WebSocketService.sendLocation(orderId, location.latitude, location.longitude, token)
                    }
                }

                // Also update via REST API for backup/logging
                val repository = (application as FoodNowApp).repository
                repository.updateLocation(location.latitude, location.longitude)
            } catch (e: Exception) {
                Log.e("LocationService", "Error sending location update", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel", 
                "Location Tracking", 
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("FoodNow Delivery")
            .setContentText("Tracking location...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        WebSocketService.disconnect()
        Log.d("LocationService", "Location tracking stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
