package com.example.foodnow

import android.app.Application
import com.example.foodnow.data.Repository
import com.example.foodnow.data.RetrofitClient
import com.example.foodnow.data.TokenManager

class FoodNowApp : Application() {
    lateinit var repository: Repository

    override fun onCreate() {
        super.onCreate()
        
        // OSMDroid configuration
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName
        
        val tokenManager = TokenManager(this)
        val apiService = RetrofitClient.getInstance(tokenManager)
        repository = Repository(apiService, tokenManager)
    }
}
