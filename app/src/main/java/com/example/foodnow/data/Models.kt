package com.example.foodnow.data

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// Auth
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String = "CLIENT" 
)

data class AuthResponse(
    val token: String,
    val id: Long,
    val email: String,
    val fullName: String,
    val role: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)



// Restaurant
data class RestaurantResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val address: String?,
    val phone: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val ownerId: Long?,
    val ownerName: String?,
    val openingHours: String?
)

// Menu
data class MenuItemResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val imageUrl: String?,
    val category: String?,
    val isAvailable: Boolean,
    val optionGroups: List<MenuOptionGroupResponse> = emptyList()
)

data class MenuOptionGroupResponse(
    val id: Long,
    val name: String,
    val isRequired: Boolean,
    val isMultiple: Boolean,
    val options: List<MenuOptionResponse>
)

data class MenuOptionResponse(
    val id: Long,
    val name: String,
    val extraPrice: BigDecimal
)

data class MenuItemRequest(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val imageUrl: String?,
    val category: String?,
    val isAvailable: Boolean,
    val optionGroups: List<MenuOptionGroupResponse> = emptyList()
)

data class RestaurantRequest(
    val name: String,
    val address: String,
    val description: String?,
    val phone: String,
    val imageUrl: String?,
    val openingHours: String?,
    // Admin fields
    val ownerEmail: String,
    val ownerPassword: String,
    val ownerFullName: String,
    val ownerPhoneNumber: String?
)

data class LivreurRequest(
    val userEmail: String,
    val userPassword: String,
    val userFullName: String,
    val userPhoneNumber: String?,
    val vehicleType: String
)

data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)

// Pagination
data class PageResponse<T>(
    val content: List<T>,
    val pageNo: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class Order(
    val id: Long,
    val restaurantName: String, // Assuming backend projection or fetch
    val status: String,
    val totalAmount: BigDecimal,
    val createdAt: String,
    @SerializedName("orderItems")
    val items: List<OrderItem> = emptyList()
)

data class DeliveryResponse(
    val id: Long,
    val orderId: Long,
    val restaurantName: String,
    val restaurantAddress: String,
    val clientName: String,
    val clientAddress: String,
    val clientPhone: String,
    val status: String,
    val pickupTime: String?,
    val deliveryTime: String?,
    val createdAt: String
)

data class LivreurResponse(
    val id: Long,
    val userId: Long,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val vehicleType: String?,
    val isAvailable: Boolean,
    val isActive: Boolean,
    val latitude: Double?,
    val longitude: Double?
)

data class LocationUpdateDto(
    val latitude: Double,
    val longitude: Double
)

data class OrderLocationResponse(
    val orderId: Long,
    val clientLatitude: Double,
    val clientLongitude: Double,
    val driverLatitude: Double?,
    val driverLongitude: Double?
)

data class OrderItem(
    val id: Long,
    val menuItemName: String,
    val quantity: Int,
    @SerializedName("unitPrice")
    val price: BigDecimal,
    val selectedOptions: List<SelectedOptionResponse>? = emptyList()
)

data class SelectedOptionResponse(
    val name: String,
    val price: BigDecimal
)

data class OrderRequest(
    val restaurantId: Long,
    val items: List<OrderItemRequest>,
    val deliveryAddress: String? = null
)

data class OrderItemRequest(
    val menuItemId: Long,
    val quantity: Int,
    val selectedOptionIds: List<Long> = emptyList()
)

data class PaymentRequest(
    val amount: BigDecimal,
    val paymentMethod: String
)

data class PaymentResponse(
    val status: String,
    val message: String
)
