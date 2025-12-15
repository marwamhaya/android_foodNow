package com.example.foodnow.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

interface ApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("/api/restaurants")
    suspend fun getAllRestaurants(): Response<PageResponse<RestaurantResponse>>

    @GET("/api/restaurants/{id}/menu")
    suspend fun getMenuItems(@Path("id") restaurantId: Long): Response<List<MenuItemResponse>>

    @GET("/api/orders/client")
    suspend fun getMyOrders(): Response<List<Order>>

    @POST("/api/orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<Order>

    @GET("/api/users/me")
    suspend fun getUserProfile(): Response<AuthResponse> // Using AuthResponse as User DTO for now

    @POST("/api/users/change-password")
    suspend fun changePassword(@Body payload: Map<String, String>): Response<Void>

    @DELETE("/api/users/me")
    suspend fun deleteAccount(): Response<Void>

    // Restaurant Specific
    @GET("/api/restaurants/my-restaurant")
    suspend fun getMyRestaurant(): Response<RestaurantResponse>

    @GET("/api/restaurants/my-restaurant/orders")
    suspend fun getMyRestaurantOrders(): Response<PageResponse<Order>>

    @POST("/api/menu-items")
    suspend fun createMenuItem(@Body request: MenuItemRequest): Response<MenuItemResponse>

    @retrofit2.http.PUT("/api/menu-items/{id}")
    suspend fun updateMenuItem(@Path("id") id: Long, @Body request: MenuItemRequest): Response<MenuItemResponse>

    @DELETE("/api/menu-items/{id}")
    suspend fun deleteMenuItem(@Path("id") id: Long): Response<Void>

    @GET("/api/menu-items/{id}")
    suspend fun getMenuItemById(@Path("id") id: Long): Response<MenuItemResponse>

    // Order Actions
    @retrofit2.http.PATCH("/api/restaurants/orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") id: Long): Response<Order>

    @retrofit2.http.PATCH("/api/restaurants/orders/{id}/prepare")
    suspend fun prepareOrder(@Path("id") id: Long): Response<Order>

    @retrofit2.http.PATCH("/api/restaurants/orders/{id}/ready")
    suspend fun readyOrder(@Path("id") id: Long): Response<Order>

    @retrofit2.http.PATCH("/api/restaurants/orders/{id}/reject")
    suspend fun rejectOrder(@Path("id") id: Long, @Body reason: Map<String, String>): Response<Order>

    // Livreur Specific
    @GET("/api/livreurs/me")
    suspend fun getLivreurProfile(): Response<LivreurResponse>

    @retrofit2.http.PATCH("/api/livreurs/availability")
    suspend fun toggleAvailability(): Response<Void>

    @GET("/api/deliveries/assigned")
    suspend fun getAssignedDeliveries(): Response<List<DeliveryResponse>>
    
    @GET("/api/deliveries/history")
    suspend fun getDeliveryHistory(): Response<List<DeliveryResponse>>

    @retrofit2.http.PATCH("/api/deliveries/{id}/status")
    suspend fun updateDeliveryStatus(@Path("id") id: Long, @Query("status") status: String): Response<DeliveryResponse>

    @retrofit2.http.PATCH("/api/driver-locations")
    suspend fun updateLocation(@Body location: LocationUpdateDto): Response<Void>

    // Admin
    @GET("/api/admin/stats")
    suspend fun getSystemStats(): Response<Map<String, Any>>

    @POST("/api/restaurants")
    suspend fun createRestaurant(@Body request: RestaurantRequest): Response<RestaurantResponse>

    @POST("/api/livreurs")
    suspend fun createLivreur(@Body request: LivreurRequest): Response<LivreurResponse>

    @GET("/api/livreurs")
    suspend fun getAllLivreurs(): Response<List<LivreurResponse>>

    @GET("/api/admin/users")
    suspend fun getAllUsers(): Response<List<User>>

    @retrofit2.http.PUT("/api/restaurants/{id}")
    suspend fun updateRestaurant(@Path("id") id: Long, @Body request: RestaurantRequest): Response<RestaurantResponse>

    @GET("/api/restaurants/{id}")
    suspend fun getRestaurantById(@Path("id") id: Long): Response<RestaurantResponse>

    @retrofit2.http.PUT("/api/livreurs/{id}")
    suspend fun updateLivreur(@Path("id") id: Long, @Body request: LivreurRequest): Response<LivreurResponse>

    @GET("/api/livreurs/{id}")
    suspend fun getLivreurById(@Path("id") id: Long): Response<LivreurResponse>

    @retrofit2.http.PATCH("/api/livreurs/{id}/status")
    suspend fun toggleLivreurStatus(@Path("id") id: Long): Response<Void>

    @GET("/api/admin/restaurants")
    suspend fun getAllRestaurantsAdmin(): Response<List<RestaurantResponse>> // Or PageResponse if strictly following Controller (which returns List in AdminController but Page in RestaurantController/admin)
    // AdminController line 39 returns List<Restaurant>. RestaurantController line 66 returns PageResponse.
    // I'll check which one I should use. Ideally RestaurantController's endpoint /api/restaurants/admin is better as it is paginated, but simpler List from AdminController is easier for UI.
    // AdminController endpoint: /api/admin/restaurants -> List<Restaurant> (Model Restaurant, not Response?)
    // I'll use AdminController endpoint for simplicity: /api/admin/restaurants
    
    @retrofit2.http.PATCH("/api/admin/users/{id}/status")
    suspend fun toggleUserStatus(@Path("id") id: Long): Response<Void>
    
    @retrofit2.http.PATCH("/api/admin/restaurants/{id}/status")
    suspend fun toggleRestaurantStatus(@Path("id") id: Long): Response<Void>

    @POST("/api/admin/users/{id}/reset-password")
    suspend fun resetUserPassword(@Path("id") id: Long, @Body payload: Map<String, String>): Response<Void>

    @GET("/api/restaurants/{id}/orders")
    suspend fun getRestaurantOrders(@Path("id") id: Long): Response<PageResponse<Order>>

    @POST("/api/payments/simulate")
    suspend fun simulatePayment(@Body request: PaymentRequest): Response<PaymentResponse>

    @GET("/api/admin/restaurants/{id}/orders/count/today")
    suspend fun getDailyOrderCount(@Path("id") id: Long): Response<Long>

    // Restaurant Orders with Status Filtering
    @GET("/api/restaurants/my-restaurant/orders/status/{status}")
    suspend fun getMyRestaurantOrdersByStatus(
        @Path("status") status: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<Order>>

    // File Upload
    @retrofit2.http.Multipart
    @POST("/api/upload/restaurant/{id}")
    suspend fun uploadRestaurantImage(
        @Path("id") id: Long,
        @retrofit2.http.Part image: okhttp3.MultipartBody.Part
    ): Response<Map<String, String>>

    @retrofit2.http.Multipart
    @POST("/api/upload/menu-item/{id}")
    suspend fun uploadMenuItemImage(
        @Path("id") id: Long,
        @retrofit2.http.Part image: okhttp3.MultipartBody.Part
    ): Response<Map<String, String>>

    // Change Password for Restaurant Users
    @retrofit2.http.PUT("/api/auth/change-password")
    suspend fun changePasswordAuth(@Body request: ChangePasswordRequest): Response<Map<String, String>>
}
