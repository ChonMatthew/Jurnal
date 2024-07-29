package com.example.jurnalapp.fragments.entry

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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

    // Declare variables for views and ViewModel
    private lateinit var mEntryViewModel: EntryViewModel

    // Declare binding variables
    private var _binding: FragmentEntryDetailBinding? = null
    private val binding get() = _binding!!

    // Declare current entry
    private var currentEntry: Entry? = null

    // Get the arguments passed from the ListFragment
    private val args by navArgs<EntryDetailFragmentArgs>()


    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEntryDetailBinding.inflate(inflater, container, false)
        // Initialize ViewModel
        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        // Get the date and time from the arguments
        val date = Date(args.currentEntry.date)
        val time = Date(args.currentEntry.time)

        // Initialize views with corresponding IDs
        val detailDateText: TextView = binding.entryDateText
        val detailTimeText: TextView = binding.entryTimeText

        // Set the date and time text with the correct context
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Set the date and time text with the correct context
        detailDateText.text = dateFormat.format(date)
        detailTimeText.text = timeFormat.format(time)

        // Set the title, subtitle, and content text with the correct context (if updated or not)
        currentEntry = args.currentEntry
        val isEntryUpdated = args.isEntryUpdated

        currentEntry?.imagePath?.let { imagePath ->
            if (imagePath.isNotEmpty()) {
                Glide.with(this)
                    .load(imagePath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Consider disabling caching for testing
                    .override(250, 250)
                    .into(binding.entryImageView)
            } else {
                binding.entryImageView.setImageDrawable(null)
            }
        }

        // checks if the entry has been updated to allow the entry arguments to be used
        if (isEntryUpdated) {
            binding.entryTitle.text = currentEntry?.title
            binding.entrySubtitle.text = currentEntry?.subtitle
            binding.entryContent.text = currentEntry?.content
        }else {
            binding.entryTitle.text = args.currentEntry.title
            binding.entrySubtitle.text = args.currentEntry.subtitle
            binding.entryContent.text = args.currentEntry.content
        }

        // checks if the entry is updated and sets the UI accordingly
        setFragmentResultListener("update_request") { _, bundle ->
            val isUpdated = bundle.getBoolean("is_updated", false)
            if (isUpdated) {
                val updatedEntry = bundle.getParcelable<Entry>("updated_entry")
                if (updatedEntry != null) {
                    currentEntry = updatedEntry

                    // Update all UI elements, including the image
                    binding.entryTitle.text = updatedEntry.title
                    binding.entrySubtitle.text = updatedEntry.subtitle
                    binding.entryContent.text = updatedEntry.content

                    // Reload the image
                    updatedEntry.imagePath?.let { imagePath ->
                        if (imagePath.isNotEmpty()) {
                            Glide.with(this)
                                .load(imagePath)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .override(250, 250)
                                .into(binding.entryImageView)
                        } else {
                            // Handle cases where there's no image
                            binding.entryImageView.setImageDrawable(null)
                        }
                    }
                }
            }
        }

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

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

    // Delete the entry from the database
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