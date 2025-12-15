package com.example.foodnow.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnow.data.LivreurRequest
import com.example.foodnow.data.Repository
import com.example.foodnow.data.RestaurantRequest
import com.example.foodnow.data.RestaurantResponse
import com.example.foodnow.data.User
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: Repository) : ViewModel() {

    private val _users = MutableLiveData<Result<List<User>>>()
    val users: LiveData<Result<List<User>>> = _users

    private val _restaurants = MutableLiveData<Result<List<RestaurantResponse>>>()
    val restaurants: LiveData<Result<List<RestaurantResponse>>> = _restaurants

    // Add orders LiveData
    private val _restaurantOrders = MutableLiveData<Result<List<com.example.foodnow.data.Order>>>()
    val restaurantOrders: LiveData<Result<List<com.example.foodnow.data.Order>>> = _restaurantOrders

    private val _restaurantDetails = MutableLiveData<Result<RestaurantResponse>>()
    val restaurantDetails: LiveData<Result<RestaurantResponse>> = _restaurantDetails

    fun getRestaurantById(id: Long) {
        viewModelScope.launch {
            try {
                // Using restaurant endpoint which seems open or we need specific admin endpoint?
                // Assuming standard endpoint works or we need to add one to repo.
                // Repository usually has getRestaurantById from user side which is public.
                val response = repository.getRestaurantById(id)
                if (response.isSuccessful && response.body() != null) {
                    _restaurantDetails.value = Result.success(response.body()!!)
                } else {
                    _restaurantDetails.value = Result.failure(Exception("Error loading details"))
                }
            } catch (e: Exception) {
                _restaurantDetails.value = Result.failure(e)
            }
        }
    }

    fun getRestaurantOrders(restaurantId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getRestaurantOrders(restaurantId)
                if (response.isSuccessful && response.body() != null) {
                    _restaurantOrders.value = Result.success(response.body()!!.content)
                } else {
                    _restaurantOrders.value = Result.failure(Exception("Error loading orders: ${response.code()}"))
                }
            } catch (e: Exception) {
                 _restaurantOrders.value = Result.failure(e)
            }
        }
    }

    fun getAllUsers() {
        viewModelScope.launch {
            try {
                val response = repository.getAllUsers()
                if (response.isSuccessful && response.body() != null) {
                    _users.value = Result.success(response.body()!!)
                } else {
                    _users.value = Result.failure(Exception("Error loading users: ${response.code()}"))
                }
            } catch (e: Exception) {
                _users.value = Result.failure(e)
            }
        }
    }

    fun getAllRestaurants() {
        viewModelScope.launch {
             try {
                 val response = repository.getAllRestaurantsAdmin()
                 if (response.isSuccessful && response.body() != null) {
                     _restaurants.value = Result.success(response.body()!!)
                 }
             } catch (e: Exception) {
                 _restaurants.value = Result.failure(e)
             }
        }
    }

    fun toggleUserStatus(id: Long) {
        viewModelScope.launch {
            repository.toggleUserStatus(id)
            getAllUsers()
        }
    }

    fun toggleRestaurantStatus(id: Long) {
        viewModelScope.launch {
            repository.toggleRestaurantStatus(id)
            getAllRestaurants()
        }
    }

    fun createRestaurant(request: RestaurantRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createRestaurant(request)
                if (response.isSuccessful) {
                    onSuccess()
                    getAllRestaurants()
                } else {
                    onError("Failed to create restaurant: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }


    fun createLivreur(request: LivreurRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createLivreur(request)
                if (response.isSuccessful) {
                    onSuccess()
                    // Refresh users? Livreur is a user + livreur entry.
                    getAllUsers()
                } else {
                    onError("Failed to create livreur: ${response.code()}")
                }
            } catch (e: Exception) {
                 onError("Error: ${e.message}")
            }
        }
    }

    fun resetUserPassword(id: Long, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.resetUserPassword(id, password)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to reset password")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    private val _livreurs = MutableLiveData<Result<List<com.example.foodnow.data.LivreurResponse>>>()
    val livreurs: LiveData<Result<List<com.example.foodnow.data.LivreurResponse>>> = _livreurs

    fun getAllLivreurs() {
        viewModelScope.launch {
            try {
                val response = repository.getAllLivreurs()
                if (response.isSuccessful && response.body() != null) {
                    _livreurs.value = Result.success(response.body()!!)
                } else {
                    _livreurs.value = Result.failure(Exception("Error loading livreurs"))
                }
            } catch (e: Exception) {
                _livreurs.value = Result.failure(e)
            }
        }
    }

    fun updateRestaurant(id: Long, request: RestaurantRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.updateRestaurant(id, request)
                if (response.isSuccessful) {
                    onSuccess()
                    getAllRestaurants() // Refresh list
                } else {
                    onError("Failed to update restaurant: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    private val _livreurDetails = MutableLiveData<Result<com.example.foodnow.data.LivreurResponse>>()
    val livreurDetails: LiveData<Result<com.example.foodnow.data.LivreurResponse>> = _livreurDetails

    fun getLivreurById(id: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getLivreurById(id)
                if (response.isSuccessful && response.body() != null) {
                    _livreurDetails.value = Result.success(response.body()!!)
                } else {
                    _livreurDetails.value = Result.failure(Exception("Error loading livreur details"))
                }
            } catch (e: Exception) {
                _livreurDetails.value = Result.failure(e)
            }
        }
    }

    fun updateLivreur(id: Long, request: LivreurRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
             try {
                val response = repository.updateLivreur(id, request)
                if (response.isSuccessful) {
                    onSuccess()
                    getAllUsers() // Refresh list (Note: getAllUsers returns all users, might need filtering logic again in fragment)
                } else {
                    onError("Failed to update livreur: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    fun toggleLivreurStatus(id: Long) {
        viewModelScope.launch {
            repository.toggleLivreurStatus(id)
            getAllLivreurs()
        }
    }

    private val _dailyOrderCount = MutableLiveData<Result<Long>>()
    val dailyOrderCount: LiveData<Result<Long>> = _dailyOrderCount

    fun getDailyOrderCount(id: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getDailyOrderCount(id)
                if (response.isSuccessful && response.body() != null) {
                    _dailyOrderCount.value = Result.success(response.body()!!)
                } else {
                    _dailyOrderCount.value = Result.failure(Exception("Error loading count"))
                }
            } catch (e: Exception) {
                _dailyOrderCount.value = Result.failure(e)
            }
        }
    }
}
