package com.example.foodnow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodnow.data.Repository

class ViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.login.LoginViewModel::class.java)) {
            return com.example.foodnow.ui.login.LoginViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.register.RegisterViewModel::class.java)) {
            return com.example.foodnow.ui.register.RegisterViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.home.HomeViewModel::class.java)) {
            return com.example.foodnow.ui.home.HomeViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.menu.MenuViewModel::class.java)) {
            return com.example.foodnow.ui.menu.MenuViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.orders.OrdersViewModel::class.java)) {
            return com.example.foodnow.ui.orders.OrdersViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.account.AccountViewModel::class.java)) {
            return com.example.foodnow.ui.account.AccountViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.restaurant.RestaurantViewModel::class.java)) {
            return com.example.foodnow.ui.restaurant.RestaurantViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.livreur.LivreurViewModel::class.java)) {
            return com.example.foodnow.ui.livreur.LivreurViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.foodnow.ui.admin.AdminViewModel::class.java)) {
            return com.example.foodnow.ui.admin.AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
