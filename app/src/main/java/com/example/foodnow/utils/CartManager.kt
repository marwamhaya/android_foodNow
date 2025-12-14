package com.example.foodnow.utils

import com.example.foodnow.data.MenuItemResponse
import com.example.foodnow.data.OrderItemRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

data class CartItem(
    val menuItem: MenuItemResponse,
    var quantity: Int,
    val selectedOptionIds: List<Long>,
    val selectedOptionsPrice: Double // Total extra price for one unit
) {
    val totalPrice: Double
        get() = (menuItem.price.toDouble() + selectedOptionsPrice) * quantity
}

object CartManager {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private var currentRestaurantId: Long? = null

    fun addItem(item: MenuItemResponse, quantity: Int, optionIds: List<Long>, optionsPrice: Double, restaurantId: Long): Boolean {
        if (currentRestaurantId != null && currentRestaurantId != restaurantId) {
            return false // Restaurant mismatch
        }
        currentRestaurantId = restaurantId

        val currentList = _cartItems.value.toMutableList()
        // Check if exact item exists (same id and same options)
        val existingItemIndex = currentList.indexOfFirst { 
            it.menuItem.id == item.id && it.selectedOptionIds == optionIds 
        }

        if (existingItemIndex != -1) {
            val existing = currentList[existingItemIndex]
            currentList[existingItemIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentList.add(CartItem(item, quantity, optionIds, optionsPrice))
        }
        _cartItems.value = currentList
        return true
    }

    fun removeItem(index: Int) {
        val currentList = _cartItems.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _cartItems.value = currentList
            if (currentList.isEmpty()) {
                currentRestaurantId = null
            }
        }
    }

    fun updateQuantity(index: Int, newQuantity: Int) {
         val currentList = _cartItems.value.toMutableList()
        if (index in currentList.indices) {
            if (newQuantity <= 0) {
                removeItem(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQuantity)
                _cartItems.value = currentList
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        currentRestaurantId = null
    }

    fun getTotal(): Double {
        return _cartItems.value.sumOf { it.totalPrice }
    }
    
    fun getOrderRequests(): List<OrderItemRequest> {
        return _cartItems.value.map { 
            OrderItemRequest(it.menuItem.id, it.quantity, it.selectedOptionIds)
        }
    }
    
    fun getCurrentRestaurantId() = currentRestaurantId
}
