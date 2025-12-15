package com.example.foodnow.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnow.data.AuthResponse
import com.example.foodnow.data.ChangePasswordRequest
import com.example.foodnow.data.Repository
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: Repository) : ViewModel() {

    private val _userProfile = MutableLiveData<Result<AuthResponse>>()
    val userProfile: LiveData<Result<AuthResponse>> = _userProfile

    private val _actionResult = MutableLiveData<Result<String>>()
    val actionResult: LiveData<Result<String>> = _actionResult

    fun fetchProfile() {
        viewModelScope.launch {
            try {
                val response = repository.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    _userProfile.value = Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            try {
                val response = repository.changePassword(current, new)
                if (response.isSuccessful) {
                    _actionResult.value = Result.success("Password changed")
                } else {
                    _actionResult.value = Result.failure(Exception("Failed to change password"))
                }
            } catch (e: Exception) {
                _actionResult.value = Result.failure(e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val response = repository.deleteAccount()
                if (response.isSuccessful) {
                    repository.logout()
                    _actionResult.value = Result.success("Account deleted")
                } else {
                    _actionResult.value = Result.failure(Exception("Failed to delete account"))
                }
            } catch (e: Exception) {
                _actionResult.value = Result.failure(e)
            }
        }
    }
    
    fun logout() {
        repository.logout()
    }
}
