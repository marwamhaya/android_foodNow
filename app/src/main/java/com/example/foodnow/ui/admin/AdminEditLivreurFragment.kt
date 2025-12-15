package com.example.foodnow.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.data.LivreurRequest
import com.example.foodnow.ui.ViewModelFactory

class AdminEditLivreurFragment : Fragment(R.layout.fragment_admin_edit_livreur) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val livreurId = arguments?.getLong("livreurId") ?: return

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPass = view.findViewById<EditText>(R.id.etPass)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val rgVehicle = view.findViewById<RadioGroup>(R.id.rgVehicle)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        viewModel.getLivreurById(livreurId)

        viewModel.livreurDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { livreur ->
                etName.setText(livreur.fullName)
                etEmail.setText(livreur.email) // Now available
                etPhone.setText(livreur.phone)
                
                // Select proper RadioButton
                when (livreur.vehicleType) {
                    "MOTO" -> rgVehicle.check(R.id.rbMoto)
                    "VELO" -> rgVehicle.check(R.id.rbVelo)
                    "SCOOTER" -> rgVehicle.check(R.id.rbScooter)
                    "VOITURE" -> rgVehicle.check(R.id.rbVoiture)
                }
            }
        }

        btnUpdate.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            val phone = etPhone.text.toString()
            
            val selectedId = rgVehicle.checkedRadioButtonId
            val vehicleType = when (selectedId) {
                R.id.rbMoto -> "MOTO"
                R.id.rbVelo -> "VELO"
                R.id.rbScooter -> "SCOOTER"
                R.id.rbVoiture -> "VOITURE"
                else -> "MOTO"
            }

            if (name.isBlank() || vehicleType.isBlank()) {
                Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LivreurRequest(
                userEmail = email, // Email should match existing or be ignored if we don't update email
                userPassword = pass,
                userFullName = name,
                userPhoneNumber = phone,
                vehicleType = vehicleType
            )

            viewModel.updateLivreur(livreurId, request,
                onSuccess = {
                    Toast.makeText(requireContext(), "Livreur updated", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
