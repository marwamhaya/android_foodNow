package com.example.foodnow.ui.livreur

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnow.data.DeliveryResponse
import com.example.foodnow.data.LivreurResponse
import com.example.foodnow.data.Repository
import kotlinx.coroutines.launch

class LivreurViewModel(private val repository: Repository) : ViewModel() {

    private val _deliveries = MutableLiveData<Result<List<DeliveryResponse>>>()
    val deliveries: LiveData<Result<List<DeliveryResponse>>> = _deliveries

    private val _profile = MutableLiveData<Result<LivreurResponse>>()
    val profile: LiveData<Result<LivreurResponse>> = _profile

    fun getAssignedDeliveries() {
        viewModelScope.launch {
            try {
                val response = repository.getAssignedDeliveries()
                if (response.isSuccessful && response.body() != null) {
                    _deliveries.value = Result.success(response.body()!!)
                } else {
                    _deliveries.value = Result.failure(Exception("Error loading deliveries: ${response.code()}"))
                }
            } catch (e: Exception) {
                _deliveries.value = Result.failure(e)
            }
        }
    }

    fun updateStatus(deliveryId: Long, status: String) {
        viewModelScope.launch {
            try {
                // Status options: "PREPARING" (no), "IN_DELIVERY", "DELIVERED"
                // Backend endpoint expects status string
                val response = repository.updateDeliveryStatus(deliveryId, status)
                if (response.isSuccessful) {
                    getAssignedDeliveries() // Refresh
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            try {
                 val response = repository.getLivreurProfile()
                 if (response.isSuccessful) {
                     _profile.value = Result.success(response.body()!!)
                 }
            } catch (e: Exception) {
                _profile.value = Result.failure(e)
            }
        }
    }
    
    fun toggleAvailability() {
         viewModelScope.launch {
             repository.toggleAvailability()
             getProfile()
         }
    }

    fun getDeliveryHistory() {
        viewModelScope.launch {
            try {
                val response = repository.getDeliveryHistory()
                if (response.isSuccessful && response.body() != null) {
                    _deliveries.value = Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                 _deliveries.value = Result.failure(e)
            }
        }
    }
    private val _availableRequests = MutableLiveData<Result<List<DeliveryResponse>>>()
    val availableRequests: LiveData<Result<List<DeliveryResponse>>> = _availableRequests
    
    private val _requestActionStatus = MutableLiveData<Result<String>>()
    val requestActionStatus: LiveData<Result<String>> = _requestActionStatus

    fun getAvailableRequests() {
        viewModelScope.launch {
            try {
                val response = repository.getAvailableDeliveryRequests()
                if (response.isSuccessful && response.body() != null) {
                    _availableRequests.value = Result.success(response.body()!!)
                } else {
                     _availableRequests.value = Result.failure(Exception("Failed to fetch requests"))
                }
            } catch (e: Exception) {
                _availableRequests.value = Result.failure(e)
            }
        }
    }

    fun acceptDeliveryRequest(requestId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.acceptDeliveryRequest(requestId)
                if (response.isSuccessful) {
                    _requestActionStatus.value = Result.success("Delivery Accepted")
                    getAvailableRequests() // Refresh list
                    getAssignedDeliveries() // Refresh assigned
                } else {
                    _requestActionStatus.value = Result.failure(Exception("Failed to accept delivery"))
                }
            } catch (e: Exception) {
                _requestActionStatus.value = Result.failure(e)
            }
        }
    }
    
    fun declineDeliveryRequest(requestId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.declineDeliveryRequest(requestId)
                if (response.isSuccessful) {
                     _requestActionStatus.value = Result.success("Delivery Declined")
                     getAvailableRequests() // Refresh list (it might disappear if we filtered it locally, but backend no-op means it stays unless we handle it locally)
                     // Ideally we remove it from the local list
                }
            } catch (e: Exception) {
                 _requestActionStatus.value = Result.failure(e)
            }
        }
    }
}
