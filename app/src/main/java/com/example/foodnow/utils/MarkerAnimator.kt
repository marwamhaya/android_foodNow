package com.example.foodnow.utils

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

object MarkerAnimator {

    fun animateMarkerToPosition(marker: Marker, target: GeoPoint, mapView: MapView, duration: Long = 1000L) {
        val start = marker.position
        
        val latAnimator = ValueAnimator.ofFloat(start.latitude.toFloat(), target.latitude.toFloat())
        val lonAnimator = ValueAnimator.ofFloat(start.longitude.toFloat(), target.longitude.toFloat())
        
        latAnimator.duration = duration
        lonAnimator.duration = duration
        latAnimator.interpolator = LinearInterpolator()
        lonAnimator.interpolator = LinearInterpolator()
        
        latAnimator.addUpdateListener { valueAnimator ->
            val lat = valueAnimator.animatedValue as Float
            val lon = lonAnimator.animatedValue as Float
            marker.position = GeoPoint(lat.toDouble(), lon.toDouble())
            mapView.invalidate()
        }
        
        latAnimator.start()
        lonAnimator.start()
    }

    fun animateCameraToPosition(mapView: MapView, target: GeoPoint, duration: Long = 1000L) {
        mapView.controller.animateTo(target, mapView.zoomLevelDouble, duration)
    }
}
