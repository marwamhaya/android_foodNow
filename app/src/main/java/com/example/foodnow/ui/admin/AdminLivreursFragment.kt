package com.example.foodnow.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodnow.FoodNowApp
import com.example.foodnow.R
import com.example.foodnow.ui.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminLivreursFragment : Fragment(R.layout.fragment_admin_livreurs) {

    private val viewModel: AdminViewModel by viewModels {
        ViewModelFactory((requireActivity().application as FoodNowApp).repository)
    }
    private lateinit var adapter: AdminLivreurAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvLivreurs)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddLivreur)

        rv.layoutManager = LinearLayoutManager(context)
        adapter = AdminLivreurAdapter(emptyList(), 
            onToggleClick = { item -> viewModel.toggleLivreurStatus(item.id) }, 
            onItemClick = { item ->
                val bundle = Bundle().apply { putLong("livreurId", item.id) }
                findNavController().navigate(R.id.action_admin_livreurs_to_edit, bundle)
            }
        )
        rv.adapter = adapter

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_admin_livreurs_to_create)
        }

        viewModel.livreurs.observe(viewLifecycleOwner) { result ->
            result.onSuccess { list -> 
                adapter.updateData(list) 
            }
            result.onFailure { e ->
                android.widget.Toast.makeText(requireContext(), "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.getAllLivreurs()
    }
}
