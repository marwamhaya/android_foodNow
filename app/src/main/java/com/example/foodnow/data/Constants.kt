package com.example.foodnow.data

object Constants {
    // Current server IP - Update this if your backend IP changes
    private const val SERVER_IP = "192.168.1.8"
    private const val PORT = "8080"
    
    const val BASE_URL = "http://$SERVER_IP:$PORT/"
    const val WS_URL = "ws://$SERVER_IP:$PORT/ws-foodnow/websocket"
}
