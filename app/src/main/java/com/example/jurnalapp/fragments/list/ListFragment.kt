package com.example.jurnalapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentListBinding
import com.example.jurnalapp.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)

        val adapter = ListAdapter()
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mUserViewModel.readAllData.observe(viewLifecycleOwner, Observer { user ->
            adapter.setData(user)
        })

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete) {
            deleteAllUsers()
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllUsers() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_,_ ->
            mUserViewModel.deleteAllUsers()
            Toast.makeText(requireContext(), "Successfully Cleared", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Clear All Entries?")
        builder.setMessage("Are You Sure You Want To Clear Your Jurnal?")
        builder.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}