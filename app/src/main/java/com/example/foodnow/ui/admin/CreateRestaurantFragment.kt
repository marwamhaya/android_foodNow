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

class CreateRestaurantFragment : Fragment(R.layout.fragment_create_restaurant) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val etName = view.findViewById<EditText>(R.id.etName)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etDesc = view.findViewById<EditText>(R.id.etDesc)
        val etOwnerName = view.findViewById<EditText>(R.id.etOwnerName)
        val etOwnerEmail = view.findViewById<EditText>(R.id.etOwnerEmail)
        val etOwnerPass = view.findViewById<EditText>(R.id.etOwnerPass)
        val etOwnerPhone = view.findViewById<EditText>(R.id.etOwnerPhone)
        val btnCreate = view.findViewById<Button>(R.id.btnCreate)
        
        btnCreate.setOnClickListener {
            val req = RestaurantRequest(
                name = etName.text.toString(),
                address = etAddress.text.toString(),
                phone = etPhone.text.toString(),
                description = etDesc.text.toString(),
                imageUrl = null,
                ownerFullName = etOwnerName.text.toString(),
                ownerEmail = etOwnerEmail.text.toString(),
                ownerPassword = etOwnerPass.text.toString(),
                ownerPhoneNumber = etOwnerPhone.text.toString(),
                openingHours = null
            )
            
            viewModel.createRestaurant(req, 
                onSuccess = {
                    Toast.makeText(context, "Restaurant Created", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
