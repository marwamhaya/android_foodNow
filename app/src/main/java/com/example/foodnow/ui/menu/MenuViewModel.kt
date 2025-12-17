package com.example.foodnow.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnow.data.MenuItemResponse
import com.example.foodnow.data.Repository
import com.example.foodnow.utils.CartManager
import kotlinx.coroutines.launch
import com.example.foodnow.data.Order
import com.example.foodnow.data.OrderItemRequest
import com.example.foodnow.data.OrderRequest

class MenuViewModel(private val repository: Repository) : ViewModel() {

    private val _menuItems = MutableLiveData<Result<List<MenuItemResponse>>>()
    val menuItems: LiveData<Result<List<MenuItemResponse>>> = _menuItems

    fun loadMenu(restaurantId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getMenuItems(restaurantId)
                if (response.isSuccessful && response.body() != null) {
                    _menuItems.value = Result.success(response.body()!!)
                } else {
                    _menuItems.value = Result.failure(Exception("Failed to load menu: ${response.code()}"))
                }
            } catch (e: Exception) {
                _menuItems.value = Result.failure(e)
            }
        }
    }

    private val _cart = MutableLiveData<MutableList<OrderItemRequest>>(mutableListOf())
    val cart: LiveData<MutableList<OrderItemRequest>> = _cart
    
    private val _orderResult = MutableLiveData<Result<Order>>()
    val orderResult: LiveData<Result<Order>> = _orderResult

    fun addToCart(item: MenuItemResponse) {
        val currentCart = _cart.value ?: mutableListOf()
        // Simple logic: If item exists, ignored for this demo, or just add new entry
        currentCart.add(OrderItemRequest(item.id, 1))
        _cart.value = currentCart
    }

    fun placeOrder(restaurantId: Long, latitude: Double, longitude: Double) {
        val items = CartManager.getOrderRequests()
        if (items.isEmpty()) return

        viewModelScope.launch {
            try {
                val request = OrderRequest(restaurantId, items, "Default Delivery Address")
                val response = repository.createOrder(request)
                if (response.isSuccessful && response.body() != null) {
                    val order = response.body()!!
                    
                    // Save client GPS location for this order
                    try {
                        val locationDto = com.example.foodnow.data.LocationUpdateDto(latitude, longitude)
                        repository.saveOrderLocation(order.id, locationDto)
                    } catch (e: Exception) {
                        // Log but don't fail the order if location save fails
                        android.util.Log.e("MenuViewModel", "Failed to save order location", e)
                    }
                    
                    _orderResult.value = Result.success(order)
                    CartManager.clearCart() // Clear global cart
                } else {
                    _orderResult.value = Result.failure(Exception("Failed to place order: ${response.code()}"))
                }
            } catch (e: Exception) {
                _orderResult.value = Result.failure(e)
            }
        }
    }

    private val _paymentResult = MutableLiveData<Result<com.example.foodnow.data.PaymentResponse>>()
    val paymentResult: LiveData<Result<com.example.foodnow.data.PaymentResponse>> = _paymentResult

    fun processPayment(amount: java.math.BigDecimal, method: String) {
        viewModelScope.launch {
            try {
                val request = com.example.foodnow.data.PaymentRequest(amount, method)
                val response = repository.simulatePayment(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "SUCCESS") {
                        _paymentResult.value = Result.success(body)
                    } else {
                        _paymentResult.value = Result.failure(Exception(body.message))
                    }
                } else {
                    _paymentResult.value = Result.failure(Exception("Payment failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                _paymentResult.value = Result.failure(e)
            }
        }
    }
}
