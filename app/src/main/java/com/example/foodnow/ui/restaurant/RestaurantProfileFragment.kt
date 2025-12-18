package com.example.foodnow.ui.restaurant

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.data.RestaurantRequest
import com.example.foodnow.ui.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class RestaurantProfileFragment : Fragment(R.layout.fragment_restaurant_profile) {

    private val viewModel: RestaurantViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    private lateinit var ivImage: ImageView
    private lateinit var etName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var etDesc: EditText
    private lateinit var etOpeningHours: EditText
    private lateinit var btnSave: Button
    private lateinit var btnUpload: View
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarImage: ProgressBar

    // Image Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uploadImage(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivImage = view.findViewById(R.id.ivRestaurantImage)
        etName = view.findViewById(R.id.etName)
        etAddress = view.findViewById(R.id.etAddress)
        etPhone = view.findViewById(R.id.etPhone)
        etDesc = view.findViewById(R.id.etDescription)
        etOpeningHours = view.findViewById(R.id.etOpeningHours)
        btnSave = view.findViewById(R.id.btnSave)
        btnUpload = view.findViewById(R.id.btnUploadImage)
        progressBar = view.findViewById(R.id.progressBar)
        progressBarImage = view.findViewById(R.id.progressBarImage)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)

        setupObservers()

        btnSave.setOnClickListener { saveProfile() }
        btnUpload.setOnClickListener { 
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        btnChangePassword.setOnClickListener {
            // Navigate to Change Password Fragment (To be implemented)
            // findNavController().navigate(R.id.action_profile_to_changePassword)
            Toast.makeText(context, "Change Password clicked", Toast.LENGTH_SHORT).show()
        }

        // Initial Load
        viewModel.getMyRestaurant()
    }

    private fun setupObservers() {
        viewModel.restaurant.observe(viewLifecycleOwner) { result ->
            result.onSuccess { restaurant ->
                etName.setText(restaurant.name)
                etAddress.setText(restaurant.address)
                etPhone.setText(restaurant.phone)
                etDesc.setText(restaurant.description)
                etOpeningHours.setText(restaurant.openingHours)
                
                if (!restaurant.imageUrl.isNullOrEmpty()) {
                    // Construct full URL if needed or handle absolute path from backend
                    // Backend returns "/uploads/..."
                    // Base URL is http://192.168.1.8:8080
                    val fullUrl = if (restaurant.imageUrl.startsWith("http")) restaurant.imageUrl 
                                  else "${com.example.foodnow.data.Constants.BASE_URL}api/restaurants/images/${restaurant.imageUrl}"
                    
                    Glide.with(this)
                        .load(fullUrl)
                        .into(ivImage)
                }
            }.onFailure {
                Toast.makeText(context, "Error loading profile: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            btnSave.isEnabled = true
            result.onSuccess { 
                if (it) Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.uploadStatus.observe(viewLifecycleOwner) { result ->
            progressBarImage.visibility = View.GONE
            result.onSuccess {
                 Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
            }.onFailure {
                 Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val name = etName.text.toString()
        val address = etAddress.text.toString()
        val phone = etPhone.text.toString()
        val desc = etDesc.text.toString()
        val openingHours = etOpeningHours.text.toString()

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(context, "Name, Address and Phone are required", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        val request = RestaurantRequest(
            name = name,
            address = address,
            description = desc,
            phone = phone,
            imageUrl = null, // Not updating image here
            openingHours = openingHours,
            ownerEmail = "", // Ignored by backend for update if empty/null (logic added)
            ownerPassword = "",
            ownerFullName = "",
            ownerPhoneNumber = null
        )
        
        viewModel.updateRestaurant(request)
    }
    
    private fun uploadImage(uri: Uri) {
        try {
            progressBarImage.visibility = View.VISIBLE
            val file = getFileFromUri(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            viewModel.uploadImage(body)
        } catch (e: Exception) {
            Toast.makeText(context, "Error preparing image: ${e.message}", Toast.LENGTH_SHORT).show()
            progressBarImage.visibility = View.GONE
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "upload_image.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return file
    }
}
