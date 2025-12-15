package com.example.foodnow.ui.restaurant

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

import androidx.fragment.app.activityViewModels

class SupplementEditorFragment : Fragment(R.layout.fragment_supplement_editor) {

    private val viewModel: RestaurantViewModel by activityViewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }
    
    private lateinit var adapter: OptionGroupAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val rvGroups = view.findViewById<RecyclerView>(R.id.rvOptionGroups)
        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddGroup)

        rvGroups.layoutManager = LinearLayoutManager(context)
        adapter = OptionGroupAdapter { group ->
            Toast.makeText(context, "Editing group: ${group.name}", Toast.LENGTH_SHORT).show()
            // Future: Navigate to detail of group or show dialog to edit group
        }
        rvGroups.adapter = adapter
        
        // Observe Draft
        viewModel.draftMenuItem.observe(viewLifecycleOwner) { item ->
            if (item != null) {
                 adapter.submitList(item.optionGroups)
            } else {
                Toast.makeText(context, "No active menu item draft!", Toast.LENGTH_SHORT).show()
                // Should probably pop back
            }
        }
        
        fab.setOnClickListener {
            showAddGroupDialog()
        }
    }
    
    private fun showAddGroupDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_group, null)
        val etName = view.findViewById<android.widget.EditText>(R.id.etGroupName)
        val cbRequired = view.findViewById<android.widget.CheckBox>(R.id.cbRequired)
        val cbMultiple = view.findViewById<android.widget.CheckBox>(R.id.cbMultiple)
        
        android.app.AlertDialog.Builder(requireContext())
             .setTitle("Add Option Group")
             .setView(view)
             .setPositiveButton("Add") { _, _ ->
                 val name = etName.text.toString()
                 if (name.isNotEmpty()) {
                     addGroup(name, cbRequired.isChecked, cbMultiple.isChecked)
                 }
             }
             .setNegativeButton("Cancel", null)
             .show()
    }
    
    private fun addGroup(name: String, required: Boolean, multiple: Boolean) {
        val newId = System.currentTimeMillis() // Temp ID for list diffing
        val newGroup = com.example.foodnow.data.MenuOptionGroupResponse(
            id = newId,
            name = name,
            isRequired = required,
            isMultiple = multiple,
            options = emptyList()
        )
        viewModel.addOptionGroupToDraft(newGroup)
    }
}
