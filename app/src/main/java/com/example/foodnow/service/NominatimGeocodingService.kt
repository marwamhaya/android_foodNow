package com.example.foodnow.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder

object NominatimGeocodingService {

    private val client = OkHttpClient()

    suspend fun geocode(address: String): GeoLocation? = withContext(Dispatchers.IO) {
        try {
            val encodedAddress = URLEncoder.encode(address, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1"
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "FoodNowApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            
            if (body != null) {
                val jsonArray = JSONArray(body)
                if (jsonArray.length() > 0) {
                    val jsonObject = jsonArray.getJSONObject(0)
                    val lat = jsonObject.getString("lat").toDouble()
                    val lon = jsonObject.getString("lon").toDouble()
                    return@withContext GeoLocation(lat, lon)
                }
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    
    data class GeoLocation(val latitude: Double, val longitude: Double)
}
