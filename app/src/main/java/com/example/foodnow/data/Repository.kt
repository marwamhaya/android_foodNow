package com.example.foodnow.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context

object RetrofitClient {
    private const val BASE_URL = "http://100.79.107.106:8080/" // Physical device - Computer's local IP

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
    
    suspend fun getMenuItems(restaurantId: Long, activeOnly: Boolean = true) = apiService.getMenuItems(restaurantId, activeOnly)
    
    suspend fun getMenuItemById(id: Long) = apiService.getMenuItemById(id)

    suspend fun getMyOrders() = apiService.getMyOrders()
    
    suspend fun createOrder(request: OrderRequest) = apiService.createOrder(request)

    suspend fun getUserProfile() = apiService.getUserProfile()

    suspend fun changePassword(current: String, new: String) = apiService.changePasswordAuth(ChangePasswordRequest(current, new))

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
    
    suspend fun getAvailableDeliveryRequests() = apiService.getAvailableDeliveryRequests()
    suspend fun acceptDeliveryRequest(id: Long) = apiService.acceptDeliveryRequest(id)
    suspend fun declineDeliveryRequest(id: Long) = apiService.declineDeliveryRequest(id)
    
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

    suspend fun getRestaurantById(id: Long) = apiService.getRestaurantById(id)
    suspend fun resetUserPassword(id: Long, password: String) = apiService.resetUserPassword(id, mapOf("newPassword" to password))
    suspend fun getAllLivreurs() = apiService.getAllLivreurs()
    suspend fun updateRestaurant(id: Long, request: RestaurantRequest) = apiService.updateRestaurant(id, request)
    suspend fun getLivreurById(id: Long) = apiService.getLivreurById(id)
    suspend fun updateLivreur(id: Long, request: LivreurRequest) = apiService.updateLivreur(id, request)
    suspend fun toggleLivreurStatus(id: Long) = apiService.toggleLivreurStatus(id)
    suspend fun getDailyOrderCount(id: Long) = apiService.getDailyOrderCount(id)
    
    // Order location methods
    suspend fun getOrderLocation(orderId: Long) = apiService.getOrderLocation(orderId)
    suspend fun saveOrderLocation(orderId: Long, location: LocationUpdateDto) = 
        apiService.saveOrderLocation(orderId, location)

    suspend fun uploadRestaurantImage(id: Long, image: okhttp3.MultipartBody.Part) = apiService.uploadRestaurantImage(id, image)
    suspend fun uploadMenuItemImage(id: Long, image: okhttp3.MultipartBody.Part) = apiService.uploadMenuItemImage(id, image)

    fun saveAuth(token: String, role: String) {
        tokenManager.saveToken(token)
        tokenManager.saveRole(role)
    }
    
    fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    fun getUserRole(): String? = tokenManager.getRole()
    
    fun logout() = tokenManager.clear()
}
