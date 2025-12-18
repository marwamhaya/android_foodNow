package com.example.foodnow.ui.restaurant

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.foodnow.FoodNowApp
import com.example.foodnow.data.Constants
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import java.math.BigDecimal

import androidx.fragment.app.activityViewModels

class AddEditMenuItemFragment : Fragment(R.layout.fragment_add_edit_menu_item) {

    private val viewModel: RestaurantViewModel by activityViewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }

    private lateinit var ivImage: android.widget.ImageView
    private lateinit var progressBarImage: ProgressBar
    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var etPrice: EditText
    private lateinit var etCategory: EditText
    private lateinit var switchAvail: com.google.android.material.switchmaterial.SwitchMaterial

    // Image Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.setDraftImage(uri)
            // Preview immediately
            Glide.with(this).load(uri).into(ivImage)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuItemId = arguments?.getLong("menuItemId", -1L) ?: -1L
        
        // Initialize Draft State
        if (savedInstanceState == null) {
            if (menuItemId != -1L) {
                viewModel.startEditing(menuItemId)
            } else {
                viewModel.startCreating()
            }
        }

        etName = view.findViewById(R.id.etName)
        etDesc = view.findViewById(R.id.etDescription)
        etPrice = view.findViewById(R.id.etPrice)
        etCategory = view.findViewById(R.id.etCategory)
        switchAvail = view.findViewById(R.id.switchAvailability)
        
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnSupplements = view.findViewById<Button>(R.id.btnManageSupplements)
        val btnUpload = view.findViewById<View>(R.id.btnUploadImage)
        ivImage = view.findViewById(R.id.ivMenuItemImage)
        progressBarImage = view.findViewById(R.id.progressBarImage)
        val tvSupplementsPreview = view.findViewById<android.widget.TextView>(R.id.tvSupplementsPreview)
        
        // Enable "Manage Supplements" only when editing an existing item
        btnSupplements.isEnabled = menuItemId > 0

        // Observe Draft State
        viewModel.draftMenuItem.observe(viewLifecycleOwner) { draft ->
             if (draft == null) return@observe
             
             // Update UI fields if they are empty (to avoid overwriting user typing if debounce is slow)
             // Or better: Only update if strictly necessary. 
             // Common pattern: Update UI only if content differs significantly or on first load.
             // Given our simple logic, we might just set them once or check focus.
             if (etName.text.toString() != draft.name) etName.setText(draft.name)
             if (etDesc.text.toString() != draft.description) etDesc.setText(draft.description)
             
             // Price formatting check
             val currentPriceStr = etPrice.text.toString()
             val draftPriceStr = draft.price.toPlainString()
             if (currentPriceStr != draftPriceStr && draft.price != BigDecimal.ZERO) {
                  etPrice.setText(draftPriceStr)
             }
             
             if (etCategory.text.toString() != draft.category) etCategory.setText(draft.category)
             if (switchAvail.isChecked != draft.isAvailable) switchAvail.isChecked = draft.isAvailable
             
             // Image logic
             if (!draft.imageUrl.isNullOrEmpty()) {
                  val fullUrl = if (draft.imageUrl.startsWith("http")) draft.imageUrl else "${Constants.BASE_URL}${draft.imageUrl}"
                  Glide.with(this).load(fullUrl).into(ivImage)
             }
             
             // Update supplements preview
             val groups = draft.optionGroups
             if (groups.isNotEmpty()) {
                 val names = groups.joinToString(", ") { it.name }
                 tvSupplementsPreview.text = "📦 ${groups.size} group(s): $names"
             } else {
                 tvSupplementsPreview.text = "No supplements added"
             }
        }

        viewModel.validationError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { success ->
                if (success) {
                    Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }.onFailure { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnSave.setOnClickListener {
            // Push current UI values to draft before saving, just in case
            syncUiToDraft()
            viewModel.saveDraft(requireContext())
        }

        btnUpload.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnSupplements.setOnClickListener {
            syncUiToDraft()
            val bundle = Bundle().apply { putLong("menuItemId", menuItemId) }
            findNavController().navigate(R.id.action_addEdit_to_supplements, bundle)
        }

        // Handle Back Press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isDraftDirty()) {
                   android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Unsaved Changes")
                        .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                        .setPositiveButton("Discard") { _, _ ->
                            viewModel.clearDraft()
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        .setNegativeButton("Keep Editing", null)
                        .show()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
    
    private fun syncUiToDraft() {
        val name = etName.text.toString()
        val desc = etDesc.text.toString()
        val price = etPrice.text.toString()
        val cat = etCategory.text.toString()
        val avail = switchAvail.isChecked
        viewModel.updateDraft(name, desc, price, cat, avail)
    }
}
