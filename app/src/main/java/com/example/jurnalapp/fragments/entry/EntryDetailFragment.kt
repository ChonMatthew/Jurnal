package com.example.jurnalapp.fragments.entry

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentEntryDetailBinding
import com.example.jurnalapp.databinding.FragmentUpdateBinding
import com.example.jurnalapp.fragments.update.UpdateFragmentArgs
import com.example.jurnalapp.model.Entry
import com.example.jurnalapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntryDetailFragment : Fragment() {

    private var _binding: FragmentEntryDetailBinding? = null
    private val binding get() = _binding!!
    private var currentEntry: Entry? = null

    private val args by navArgs<EntryDetailFragmentArgs>()

    private lateinit var mEntryViewModel: EntryViewModel

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEntryDetailBinding.inflate(inflater, container, false)
//        val view =  inflater.inflate(R.layout.fragment_update, container, false)

        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        val detailDateText: TextView = binding.entryDateText
        val detailTimeText: TextView = binding.entryTimeText

        // Assuming you have the Entry object available as 'args.currentEntry'
        val date = Date(args.currentEntry.date)
        val time = Date(args.currentEntry.time)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        detailDateText.text = dateFormat.format(date)
        detailTimeText.text = timeFormat.format(time)

        currentEntry = args.currentEntry
        val isEntryUpdated = args.isEntryUpdated

        if (isEntryUpdated) {
            binding.entryTitle.text = currentEntry?.title
            binding.entrySubtitle.text = currentEntry?.subtitle
            binding.entryContent.text = currentEntry?.content
        }else {
            binding.entryTitle.text = args.currentEntry.title
            binding.entrySubtitle.text = args.currentEntry.subtitle
            binding.entryContent.text = args.currentEntry.content
        }

//        binding.entryTitle.setText(args.currentEntry.title)
//        binding.entrySubtitle.setText(args.currentEntry.subtitle)
//        binding.entryContent.setText(args.currentEntry.content)

//        binding.UpdateButton.setOnClickListener {
//            updateItem()
//        }

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        setFragmentResultListener("update_request") { _, bundle ->
            val isUpdated = bundle.getBoolean("is_updated", false)

            if(isUpdated) {
                val updatedEntry = bundle.getParcelable<Entry>("updated_entry")
                if (updatedEntry != null) {
                    currentEntry = updatedEntry
                    // Update the UI with the updatedEntry data
                    binding.entryTitle.text = updatedEntry.title
                    binding.entrySubtitle.text = updatedEntry.subtitle
                    binding.entryContent.text = updatedEntry.content
                }
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.entry_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete){
            deleteEntry()
        } else if (item.itemId == R.id.menu_edit) {
            val updatedEntry = currentEntry
            if(updatedEntry != null) {
                val action =
                    EntryDetailFragmentDirections.actionEntryDetailFragmentToUpdateFragment(
                        updatedEntry
                    )
                findNavController().navigate(action)
            }
        } else {
            val action =
                EntryDetailFragmentDirections.actionEntryDetailFragmentToListFragment()
            findNavController().navigate(action)
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    private fun deleteEntry() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_,_ ->
            mEntryViewModel.deleteEntry(args.currentEntry)
            Toast.makeText(requireContext(), "Successfully Removed", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_entryDetailFragment_to_listFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete ${args.currentEntry.title}?")
        builder.setMessage("Are You Sure You Want To Delete ${args.currentEntry.title}")
        builder.create().show()
    }
}