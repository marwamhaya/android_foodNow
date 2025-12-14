package com.example.foodnow.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // Emulator localhost

    fun getInstance(tokenManager: TokenManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}

class Repository(private val apiService: ApiService, private val tokenManager: TokenManager) {

    suspend fun login(request: LoginRequest) = apiService.login(request)
    
    suspend fun register(request: RegisterRequest) = apiService.register(request)
    
    suspend fun getRestaurants(): Response<PageResponse<RestaurantResponse>> = apiService.getAllRestaurants()
    
    suspend fun getMenuItems(restaurantId: Long) = apiService.getMenuItems(restaurantId)

    suspend fun getMyOrders() = apiService.getMyOrders()
    
    suspend fun createOrder(request: OrderRequest) = apiService.createOrder(request)

    suspend fun getUserProfile() = apiService.getUserProfile()

    suspend fun changePassword(password: String) = apiService.changePassword(mapOf("newPassword" to password))

    suspend fun deleteAccount() = apiService.deleteAccount()

    // Restaurant methods
    suspend fun getMyRestaurant() = apiService.getMyRestaurant()
    suspend fun getMyRestaurantOrders() = apiService.getMyRestaurantOrders()
    suspend fun createMenuItem(request: MenuItemRequest) = apiService.createMenuItem(request)
    suspend fun updateMenuItem(id: Long, request: MenuItemRequest) = apiService.updateMenuItem(id, request)
    suspend fun deleteMenuItem(id: Long) = apiService.deleteMenuItem(id)
    suspend fun acceptOrder(id: Long) = apiService.acceptOrder(id)
    suspend fun prepareOrder(id: Long) = apiService.prepareOrder(id)
    suspend fun readyOrder(id: Long) = apiService.readyOrder(id)
    suspend fun rejectOrder(id: Long, reason: String) = apiService.rejectOrder(id, mapOf("reason" to reason))

    // Livreur methods
    suspend fun getLivreurProfile() = apiService.getLivreurProfile()
    suspend fun toggleAvailability() = apiService.toggleAvailability()
    suspend fun getAssignedDeliveries() = apiService.getAssignedDeliveries()
    suspend fun getDeliveryHistory() = apiService.getDeliveryHistory()
    suspend fun updateDeliveryStatus(id: Long, status: String) = apiService.updateDeliveryStatus(id, status)
    suspend fun updateLocation(lat: Double, lng: Double) = apiService.updateLocation(LocationUpdateDto(lat, lng))

    // Admin methods
    suspend fun getSystemStats() = apiService.getSystemStats()
    suspend fun createRestaurant(request: RestaurantRequest) = apiService.createRestaurant(request)
    suspend fun createLivreur(request: LivreurRequest) = apiService.createLivreur(request)
    suspend fun getAllUsers() = apiService.getAllUsers()
    suspend fun getAllRestaurantsAdmin() = apiService.getAllRestaurantsAdmin()
    suspend fun toggleUserStatus(id: Long) = apiService.toggleUserStatus(id)
    suspend fun toggleRestaurantStatus(id: Long) = apiService.toggleRestaurantStatus(id)
    suspend fun getRestaurantOrders(id: Long) = apiService.getRestaurantOrders(id) 
    
    suspend fun simulatePayment(request: PaymentRequest) = apiService.simulatePayment(request)

    fun saveAuth(token: String, role: String) {
        tokenManager.saveToken(token)
        tokenManager.saveRole(role)
    }
    
    fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    fun getUserRole(): String? = tokenManager.getRole()
    
    fun logout() = tokenManager.clear()
}
