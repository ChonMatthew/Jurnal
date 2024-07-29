package com.example.jurnalapp.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentListBinding
import com.example.jurnalapp.databinding.FragmentSearchBinding
import com.example.jurnalapp.fragments.list.ListAdapter
import com.example.jurnalapp.viewmodel.EntryViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var mEntryViewModel: EntryViewModel
    private lateinit var adapter: ListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        adapter = ListAdapter()
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        val searchView = binding.searchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchDatabase(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchDatabase(newText)
                }
                return true
            }
        })

        searchView.setOnClickListener {
            searchView.isIconified = false // Expand the SearchView
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun searchDatabase(query: String) {
        val searchQuery = "%$query%"

        mEntryViewModel.searchDatabase(searchQuery).observe(viewLifecycleOwner) { list ->
            list.let {
                adapter.setData(it)
            }
        }
    }
}

