package com.example.foodnow.service

import android.util.Log
import com.google.gson.Gson
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

object WebSocketService {
    private const val TAG = "WebSocketService"
    private const val WS_URL = com.example.foodnow.data.Constants.WS_URL
    
    private var stompClient: StompClient? = null
    private val activeSubscriptions = mutableMapOf<String, io.reactivex.disposables.Disposable>()
    private val gson = Gson()
    
    private var isConnecting = false
    private var reconnectAttempt = 0
    private val MAX_RECONNECT_ATTEMPTS = 10
    private var lastToken: String? = null

    fun connect(token: String? = null) {
        if (stompClient != null && stompClient!!.isConnected) return
        if (isConnecting) return
        
        lastToken = token ?: lastToken
        isConnecting = true
        
        try {
            val clientBuilder = okhttp3.OkHttpClient.Builder()
            if (lastToken != null) {
                clientBuilder.addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Authorization", "Bearer $lastToken")
                        .build()
                    chain.proceed(request)
                }
            }
            
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL, null, clientBuilder.build())
            
            stompClient?.lifecycle()?.subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        Log.d(TAG, "Stomp connection opened")
                        isConnecting = false
                        reconnectAttempt = 0
                        // Re-subscribe to all active topics
                        val topics = activeSubscriptions.keys.toList()
                        activeSubscriptions.clear()
                        topics.forEach { subscribeToTopic(it, lastToken) { _ -> } }
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                        Log.d(TAG, "Stomp connection closed")
                        isConnecting = false
                        handleReconnect()
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                        Log.e(TAG, "Stomp connection error", lifecycleEvent.exception)
                        isConnecting = false
                        handleReconnect()
                    }
                    else -> Log.d(TAG, "Stomp lifecycle event: ${lifecycleEvent.type}")
                }
            }
            
            stompClient?.connect()
            Log.d(TAG, "Connecting to WebSocket with token present: ${lastToken != null}")
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to WebSocket", e)
            isConnecting = false
            handleReconnect()
        }
    }

    private fun handleReconnect() {
        if (reconnectAttempt < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempt++
            val delay = Math.min(1000L * Math.pow(2.0, reconnectAttempt.toDouble()).toLong(), 30000L)
            Log.d(TAG, "Scheduling reconnect attempt $reconnectAttempt in ${delay}ms")
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                connect()
            }, delay)
        }
    }

    fun subscribeToRestaurantOrders(ownerId: Long, token: String, onUpdate: () -> Unit) {
        subscribeToTopic("/topic/restaurant/$ownerId/orders", token, onUpdate)
    }

    fun subscribeToAvailableDeliveries(token: String, onUpdate: () -> Unit) {
        subscribeToTopic("/topic/deliveries/available", token, onUpdate)
    }

    fun subscribeToTopic(topic: String, token: String? = null, onUpdate: (String) -> Unit) {
        if (stompClient == null || !stompClient!!.isConnected) {
            connect(token)
            // Even if not connected now, we'll re-subscribe in OPENED event
            activeSubscriptions[topic] = io.reactivex.disposables.Disposables.empty()
            return
        }

        if (activeSubscriptions.containsKey(topic) && activeSubscriptions[topic]?.isDisposed == false) {
            Log.d(TAG, "Already subscribed to topic: $topic")
            return
        }

        val disposable = stompClient?.topic(topic)?.subscribe(
            { topicMessage ->
                Log.d(TAG, "Message from $topic: ${topicMessage.payload}")
                onUpdate(topicMessage.payload)
            },
            { error -> 
                Log.e(TAG, "Error subscribing to $topic", error)
                activeSubscriptions.remove(topic)
            }
        )
        
        if (disposable != null) {
            activeSubscriptions[topic] = disposable
        }
    }

    // Overload for simple refresh triggers
    fun subscribeToTopic(topic: String, token: String? = null, onUpdate: () -> Unit) {
        subscribeToTopic(topic, token) { _ -> onUpdate() }
    }

    fun sendLocation(orderId: Long, latitude: Double, longitude: Double, token: String? = null) {
        if (stompClient == null || !stompClient!!.isConnected) {
            connect(token)
            return
        }

        val locationDto = LocationUpdateDto(latitude, longitude)
        val json = gson.toJson(locationDto)
        
        Log.d(TAG, "Sending location for order $orderId: $json")
        stompClient?.send("/app/delivery/$orderId/location", json)?.subscribe(
            { Log.d(TAG, "Location sent successfully") },
            { error -> Log.e(TAG, "Error sending location", error) }
        )
    }

    fun disconnect() {
        activeSubscriptions.values.forEach { it.dispose() }
        activeSubscriptions.clear()
        stompClient?.disconnect()
        stompClient = null
    }

    data class LocationUpdateDto(
        val latitude: Double,
        val longitude: Double
    )
}
