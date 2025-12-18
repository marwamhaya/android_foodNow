package com.example.foodnow.utils

import org.osmdroid.util.GeoPoint
import kotlin.math.roundToInt

object NavigationHelper {

    fun calculateDistance(start: GeoPoint, end: GeoPoint): Double {
        return start.distanceToAsDouble(end) / 1000.0 // Returns distance in km
    }

    fun calculateETA(distanceKm: Double): Int {
        // Assume average speed of 30 km/h in city
        val speedKmH = 30.0
        val timeHours = distanceKm / speedKmH
        return (timeHours * 60).roundToInt() // Returns minutes
    }

    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1) {
            "${(distanceKm * 1000).roundToInt()} m"
        } else {
            String.format("%.1f km", distanceKm)
        }
    }

    fun formatETA(minutes: Int): String {
        return if (minutes < 1) {
            "Less than a minute"
        } else {
            "$minutes min"
        }
    }
}
