package com.example.foodnow.ui.restaurant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnow.data.MenuItemRequest
import com.example.foodnow.data.Order
import com.example.foodnow.data.Repository
import com.example.foodnow.data.RestaurantResponse

import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class RestaurantViewModel(private val repository: Repository) : ViewModel() {

    private val _restaurant = MutableLiveData<Result<RestaurantResponse>>()
    val restaurant: LiveData<Result<RestaurantResponse>> = _restaurant

    private val _orders = MutableLiveData<Result<List<Order>>>()
    val orders: LiveData<Result<List<Order>>> = _orders

    private val _menuItems = MutableLiveData<Result<List<com.example.foodnow.data.MenuItemResponse>>>()
    val menuItems: LiveData<Result<List<com.example.foodnow.data.MenuItemResponse>>> = _menuItems

    var currentRestaurantId: Long? = null
    
    // --- Draft State for Menu Item ---
    private val _draftMenuItem = MutableLiveData<MenuItemRequest?>()
    val draftMenuItem: LiveData<MenuItemRequest?> = _draftMenuItem
    
    private var _draftId: Long = -1L // -1 for new, >0 for edit
    private var _draftImageUri: android.net.Uri? = null // Temporary local URI
    
    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError
    
    private val _saveStatus = MutableLiveData<Result<Boolean>>()
    val saveStatus: LiveData<Result<Boolean>> = _saveStatus

    // Check if draft is dirty (simplistic verify against initial state could be added, 
    // for now just checks if we have active draft data)
    fun isDraftDirty(): Boolean {
        // True if we have a draft and users might have typed something.
        // For distinct "isDirty", we'd need to compare with original. 
        // For this task, we assume entering the screen starts a session.
        return _draftMenuItem.value != null
    }

    fun getDraftId(): Long = _draftId

    fun clearDraft() {
        _draftMenuItem.value = null
        _draftId = -1L
        _draftImageUri = null
        _validationError.value = null
    }

    fun startCreating() {
        if (_draftId == -1L && _draftMenuItem.value != null) return // Already creating
        
        _draftId = -1L
        _draftImageUri = null
        _draftMenuItem.value = MenuItemRequest(
            name = "",
            description = "",
            price = java.math.BigDecimal.ZERO,
            imageUrl = null,
            category = "",
            isAvailable = true,
            optionGroups = emptyList()
        )
        _validationError.value = null
        _saveStatus.value = Result.success(false)
    }

    fun startEditing(id: Long) {
        if (_draftId == id && _draftMenuItem.value != null) return // Already editing this item
        
        val item = _menuItems.value?.getOrNull()?.find { it.id == id }
        if (item != null) {
            _draftId = id
            _draftImageUri = null
            _draftMenuItem.value = MenuItemRequest(
                name = item.name,
                description = item.description,
                price = item.price,
                imageUrl = item.imageUrl,
                category = item.category,
                isAvailable = item.isAvailable,
                optionGroups = item.optionGroups
            )
        }
        _validationError.value = null
    }
    
    fun updateDraft(name: String, desc: String, priceStr: String, category: String, isAvailable: Boolean) {
        val current = _draftMenuItem.value ?: return
        val price = if (priceStr.isNotEmpty()) try { java.math.BigDecimal(priceStr) } catch(e:Exception) { java.math.BigDecimal.ZERO } else java.math.BigDecimal.ZERO
        
        _draftMenuItem.value = current.copy(
            name = name,
            description = desc,
            price = price,
            category = category,
            isAvailable = isAvailable
        )
    }

    fun setDraftImage(uri: android.net.Uri) {
        _draftImageUri = uri
        // We do NOT update imageUrl in draftRequest yet, because we don't have a backend URL.
        // The UI should show this local URI.
        // We trigger an update just to refresh observers if needed, 
        // but primarily the Fragment holds the URI too or observes a separate "draftImage" if we made one.
        // To simplify, let's keep it in Fragment or add a LiveData here.
    }
    
    fun addOptionGroupToDraft(group: com.example.foodnow.data.MenuOptionGroupResponse) {
        val current = _draftMenuItem.value ?: return
        val newGroups = current.optionGroups.toMutableList()
        newGroups.add(group)
        _draftMenuItem.value = current.copy(optionGroups = newGroups)
    }

    fun saveDraft(context: android.content.Context) {
        val draft = _draftMenuItem.value ?: return
        
        // 1. Validation
        if (draft.name.isBlank()) {
            _validationError.value = "Name is required"
            return
        }
        if (draft.price <= java.math.BigDecimal.ZERO) {
              _validationError.value = "Price must be greater than 0"
              return
        }
        if (draft.category.isNullOrBlank()) {
             _validationError.value = "Category is required"
             return
        }
        
        viewModelScope.launch {
            try {
                var finalImageUrl = draft.imageUrl
                
                // 2. Upload Image if new one selected
                if (_draftImageUri != null) {
                    // Similar logic to previous uploadMenuItemImage but synchronous here
                     val inputStream = context.contentResolver.openInputStream(_draftImageUri!!)
                     val file = java.io.File(context.cacheDir, "temp_upload.jpg")
                     java.io.FileOutputStream(file).use { it.write(inputStream?.readBytes()) }
                     val mediaType = "image/*".toMediaTypeOrNull()
                     val requestFile = okhttp3.RequestBody.create(mediaType, file)
                     val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
                     
                     if (currentRestaurantId == null) {
                         _validationError.value = "Restaurant ID missing"
                         return@launch
                     }
                     
                     // We need a generic upload endpoint. The previous one required menuItemId which we might not have.
                     // IMPORTANT: The backend API `uploadMenuItemImage` takes an ID. 
                     // If we are CREATING, we don't have an ID yet. 
                     // Strategy: Create item first -> Get ID -> Upload Image.
                     // OR used Restaurant Image upload? No that's for profile.
                     // If backend doesn't support "upload without ID", we must create item first.
                     // Let's assume we Create then Upload.
                     
                }
                
                // 3. Create or Update Item
                if (_draftId == -1L) {
                    val createResponse = repository.createMenuItem(draft)
                    if (createResponse.isSuccessful && createResponse.body() != null) {
                         // Item Created. Now Upload Image if needed using the new ID.
                         val createdId = createResponse.body()!!.id
                         if (_draftImageUri != null) {
                             uploadImageForMenuItem(context, createdId, _draftImageUri!!)
                         } else {
                             _saveStatus.value = Result.success(true)
                             getMenuItems()
                         }
                    } else {
                        _saveStatus.value = Result.failure(Exception("Creation failed: ${createResponse.code()}"))
                    }
                } else {
                    // Updating
                    // If we have a new image, we might need to upload it first or after? 
                    // `uploadMenuItemImage` in the original code updates the item's image URL on the server side?
                    // Yes, usually. So we can update standard fields, then upload image.
                    val updateResponse = repository.updateMenuItem(_draftId, draft)
                    if (updateResponse.isSuccessful) {
                         if (_draftImageUri != null) {
                             uploadImageForMenuItem(context, _draftId, _draftImageUri!!)
                         } else {
                             _saveStatus.value = Result.success(true)
                             getMenuItems()
                         }
                    } else {
                        _saveStatus.value = Result.failure(Exception("Update failed: ${updateResponse.code()}"))
                    }
                }
            } catch (e: Exception) {
                _saveStatus.value = Result.failure(e)
            }
        }
    }
    
    private suspend fun uploadImageForMenuItem(context: android.content.Context, id: Long, uri: android.net.Uri) {
         try {
             val inputStream = context.contentResolver.openInputStream(uri)
             val file = java.io.File(context.cacheDir, "upload_$id.jpg")
             java.io.FileOutputStream(file).use { it.write(inputStream?.readBytes()) }
             val mediaType = "image/*".toMediaTypeOrNull()
             val requestFile = okhttp3.RequestBody.create(mediaType, file)
             val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
             
             val response = repository.uploadMenuItemImage(id, body)
             if (response.isSuccessful) {
                 _saveStatus.value = Result.success(true)
                 getMenuItems()
             } else {
                 _saveStatus.value = Result.failure(Exception("Image Upload failed: ${response.code()}"))
             }
         } catch (e: Exception) {
             _saveStatus.value = Result.failure(e)
         }
    }

    fun getMyRestaurant() {
        viewModelScope.launch {
            try {
                val response = repository.getMyRestaurant()
                if (response.isSuccessful && response.body() != null) {
                    val rest = response.body()!!
                    currentRestaurantId = rest.id
                    _restaurant.value = Result.success(rest)
                    // Auto load menu
                    getMenuItems()
                } else {
                    _restaurant.value = Result.failure(Exception("Error fetching restaurant: ${response.code()}"))
                }
            } catch (e: Exception) {
                _restaurant.value = Result.failure(e)
            }
        }
    }

    fun getMenuItems() {
        if (currentRestaurantId == null) return
        viewModelScope.launch {
            try {
                // Fetch all items (active and inactive) for the owner
                val response = repository.getMenuItems(currentRestaurantId!!, false)
                if (response.isSuccessful && response.body() != null) {
                    _menuItems.value = Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                 _menuItems.value = Result.failure(e)
            }
        }
    }

    fun createMenuItem(request: MenuItemRequest) {
        viewModelScope.launch {
            try {
                val response = repository.createMenuItem(request)
                if (response.isSuccessful) {
                    getMenuItems() // Refresh
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }
    
    fun updateMenuItem(id: Long, request: MenuItemRequest) {
        viewModelScope.launch {
            try {
                val response = repository.updateMenuItem(id, request)
                if (response.isSuccessful) {
                    getMenuItems() // Refresh
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun uploadMenuItemImage(id: Long, part: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response = repository.uploadMenuItemImage(id, part)
                if (response.isSuccessful && response.body() != null) {
                   val url = response.body()!!["imageUrl"]
                   if (url != null) {
                       // Update locally or refresh - refresh is safer
                       getMenuItems()
                       _uploadStatus.value = Result.success(url)
                   }
                } else {
                     _uploadStatus.value = Result.failure(Exception("Upload failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                _uploadStatus.value = Result.failure(e)
            }
        }
    }

    fun deleteMenuItem(id: Long) {
        viewModelScope.launch {
             repository.deleteMenuItem(id)
             getMenuItems()
        }
    }

    fun getOrders() {
        viewModelScope.launch {
            try {
                // Currently fetching all orders, backend supports filtering by status if needed
                val response = repository.getMyRestaurantOrders()
                if (response.isSuccessful && response.body() != null) {
                    _orders.value = Result.success(response.body()!!.content)
                } else {
                    _orders.value = Result.failure(Exception("Error fetching orders: ${response.code()}"))
                }
            } catch (e: Exception) {
                _orders.value = Result.failure(e)
            }
        }
    }

    private val _orderActionStatus = MutableLiveData<Result<String>>()
    val orderActionStatus: LiveData<Result<String>> = _orderActionStatus

    fun acceptOrder(orderId: Long) {
        android.util.Log.d("RestaurantVM", "acceptOrder called for $orderId")
        viewModelScope.launch {
            try {
                val response = repository.acceptOrder(orderId)
                android.util.Log.d("RestaurantVM", "acceptOrder response: ${response.code()}")
                if (response.isSuccessful) {
                    _orderActionStatus.value = Result.success("Order Accepted")
                    getOrders() // Refresh
                } else {
                    _orderActionStatus.value = Result.failure(Exception("Failed to accept order: ${response.code()}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("RestaurantVM", "acceptOrder exception", e)
                _orderActionStatus.value = Result.failure(e)
            }
        }
    }

    fun prepareOrder(orderId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.prepareOrder(orderId)
                if (response.isSuccessful) {
                    _orderActionStatus.value = Result.success("Order Preparing")
                    getOrders()
                } else {
                    _orderActionStatus.value = Result.failure(Exception("Failed to prepare order: ${response.code()}"))
                }
            } catch (e: Exception) {
                _orderActionStatus.value = Result.failure(e)
            }
        }
    }

    fun readyOrder(orderId: Long) {
        viewModelScope.launch {
            try {
               val response = repository.readyOrder(orderId)
               if (response.isSuccessful) {
                   _orderActionStatus.value = Result.success("Order Ready")
                   getOrders()
               } else {
                   _orderActionStatus.value = Result.failure(Exception("Failed to mark ready: ${response.code()}"))
               }
            } catch (e: Exception) {
                _orderActionStatus.value = Result.failure(e)
            }
        }
    }

    private val _selectedMenuItem = MutableLiveData<com.example.foodnow.data.MenuItemResponse?>()
    val selectedMenuItem: LiveData<com.example.foodnow.data.MenuItemResponse?> = _selectedMenuItem

    fun loadMenuItemById(id: Long) {
        // Try cache first
        val item = _menuItems.value?.getOrNull()?.find { it.id == id }
        if (item != null) {
            _selectedMenuItem.value = item
        } else {
            // Fallback: fetch from API
            viewModelScope.launch {
                try {
                    val response = repository.getMenuItemById(id)
                    if (response.isSuccessful && response.body() != null) {
                        _selectedMenuItem.value = response.body()
                    }
                } catch (e: Exception) {
                    // Handle error silently or log
                }
            }
        }
    }
    
    fun updateMenuItemLocal(updatedItem: com.example.foodnow.data.MenuItemResponse) {
        _selectedMenuItem.value = updatedItem
        // Sync with backend
        val request = MenuItemRequest(
            name = updatedItem.name,
            description = updatedItem.description,
            price = updatedItem.price,
            imageUrl = updatedItem.imageUrl,
            category = updatedItem.category,
            isAvailable = updatedItem.isAvailable,
            optionGroups = updatedItem.optionGroups
        )
        updateMenuItem(updatedItem.id, request)
    }

    fun rejectOrder(orderId: Long, reason: String) {
        viewModelScope.launch {
            try {
                val response = repository.rejectOrder(orderId, reason)
                if (response.isSuccessful) {
                    _orderActionStatus.value = Result.success("Order Declined")
                    getOrders()
                } else {
                    _orderActionStatus.value = Result.failure(Exception("Failed to decline order: ${response.code()}"))
                }
            } catch (e: Exception) {
                _orderActionStatus.value = Result.failure(e)
            }
        }
    }

    private val _updateStatus = MutableLiveData<Result<Boolean>>()
    val updateStatus: LiveData<Result<Boolean>> = _updateStatus

    private val _uploadStatus = MutableLiveData<Result<String>>()
    val uploadStatus: LiveData<Result<String>> = _uploadStatus

    fun updateRestaurant(request: com.example.foodnow.data.RestaurantRequest) {
        viewModelScope.launch {
            try {
                if (currentRestaurantId == null) return@launch
                val response = repository.updateRestaurant(currentRestaurantId!!, request)
                if (response.isSuccessful && response.body() != null) {
                    _restaurant.value = Result.success(response.body()!!)
                    _updateStatus.value = Result.success(true)
                } else {
                    _updateStatus.value = Result.failure(Exception("Update failed: ${response.code()}"))
                }
            } catch (e: Exception) {
               _updateStatus.value = Result.failure(e)
            }
        }
    }
    
    fun uploadImage(part: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
             try {
                if (currentRestaurantId == null) return@launch
                val response = repository.uploadRestaurantImage(currentRestaurantId!!, part)
                if (response.isSuccessful && response.body() != null) {
                    val url = response.body()!!["imageUrl"]
                    if (url != null) {
                        _uploadStatus.value = Result.success(url)
                        getMyRestaurant() // Refresh to update model
                    }
                } else {
                    _uploadStatus.value = Result.failure(Exception("Upload failed: ${response.code()}"))
                }
             } catch (e: Exception) {
                 _uploadStatus.value = Result.failure(e)
             }
        }
    }
    
    private val _passwordChangeStatus = MutableLiveData<Result<Boolean>>()
    val passwordChangeStatus: LiveData<Result<Boolean>> = _passwordChangeStatus

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            try {
                val response = repository.changePassword(current, new)
                if (response.isSuccessful) {
                    _passwordChangeStatus.value = Result.success(true)
                } else {
                    _passwordChangeStatus.value = Result.failure(Exception("Failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                _passwordChangeStatus.value = Result.failure(e)
            }
        }
    }
}
