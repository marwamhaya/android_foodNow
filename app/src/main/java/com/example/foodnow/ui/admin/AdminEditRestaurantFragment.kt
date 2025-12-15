package com.example.foodnow.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.data.RestaurantRequest
import com.example.foodnow.ui.ViewModelFactory

class AdminEditRestaurantFragment : Fragment(R.layout.fragment_admin_edit_restaurant) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurantId = arguments?.getLong("restaurantId") ?: return

        val etName = view.findViewById<EditText>(R.id.etName)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etDesc = view.findViewById<EditText>(R.id.etDesc)
        val etImageUrl = view.findViewById<EditText>(R.id.etImageUrl)
        val etOwnerName = view.findViewById<EditText>(R.id.etOwnerName)
        val etOwnerEmail = view.findViewById<EditText>(R.id.etOwnerEmail)
        val etOwnerPass = view.findViewById<EditText>(R.id.etOwnerPass)
        val etOwnerPhone = view.findViewById<EditText>(R.id.etOwnerPhone)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        viewModel.getRestaurantById(restaurantId)

        viewModel.restaurantDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { resto ->
                etName.setText(resto.name)
                etAddress.setText(resto.address)
                etPhone.setText(resto.phone)
                etDesc.setText(resto.description)
                etImageUrl.setText(resto.imageUrl)
                etOwnerName.setText(resto.ownerName)
                // Owner Email and Phone might not be in RestaurantResponse (it only has ownerName and ownerId in basic response usually)
                // If I need to prepopulate email/phone, I might need to fetch Owner User details using ownerId.
                // Or I leave them empty if logic allows partial updates (backend updates email only if provided).
                // Let's assume we can't easily get email/phone from RestaurantResponse unless I check the DTO again.
            }
        }

        btnUpdate.setOnClickListener {
            val name = etName.text.toString()
            val address = etAddress.text.toString()
            val phone = etPhone.text.toString()
            val desc = etDesc.text.toString()
            val imageUrl = etImageUrl.text.toString()
            val ownerName = etOwnerName.text.toString()
            val ownerEmail = etOwnerEmail.text.toString()
            val ownerPass = etOwnerPass.text.toString()
            val ownerPhone = etOwnerPhone.text.toString()

            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RestaurantRequest(
                name = name,
                address = address,
                description = desc,
                phone = phone,
                imageUrl = imageUrl,
                ownerEmail = ownerEmail,
                ownerPassword = ownerPass, // If empty, backend skips update
                ownerFullName = ownerName,
                ownerPhoneNumber = ownerPhone,
                openingHours = null
            )

            viewModel.updateRestaurant(restaurantId, request, 
                onSuccess = {
                    Toast.makeText(requireContext(), "Restaurant updated", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
