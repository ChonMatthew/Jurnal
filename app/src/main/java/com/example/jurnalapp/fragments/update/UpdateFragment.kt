package com.example.jurnalapp.fragments.update

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.jurnalapp.R
import com.example.jurnalapp.data.handler.ImageHandler
import com.example.jurnalapp.data.model.Entry
import com.example.jurnalapp.data.util.DateTimeUtil
import com.example.jurnalapp.data.viewmodel.EntryViewModel
import com.example.jurnalapp.databinding.FragmentUpdateBinding
import java.util.Calendar

class UpdateFragment : Fragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var entryDateText: TextView
    private lateinit var entryTimeText: TextView
    private lateinit var mEntryViewModel: EntryViewModel

    private var selectedDateInMillis = 0L
    private var selectedTimeInMillis = 0L

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<UpdateFragmentArgs>()

    private lateinit var imageHandler: ImageHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        setupUI()
        initializeImageHandler()

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setupUI() {
        binding.updateTitle.setText(args.currentEntry.title)
        binding.updateSubtitle.setText(args.currentEntry.subtitle)
        binding.updateContent.setText(args.currentEntry.content)

        selectedDateInMillis = args.currentEntry.date
        selectedTimeInMillis = args.currentEntry.time
        entryDateText = binding.entryDateText
        entryTimeText = binding.entryTimeText

        updateDateTimeText()

        binding.updateDateButton.setOnClickListener {
            DateTimeUtil.showDatePicker(requireContext()) { timeInMillis ->
                selectedDateInMillis = timeInMillis
                updateDateTimeText()
            }
        }

        binding.updateTimeButton.setOnClickListener {
            DateTimeUtil.showTimePicker(requireContext()) { timeInMillis ->
                selectedTimeInMillis = timeInMillis
                updateDateTimeText()
            }
        }

        args.currentEntry.imagePath?.let { imagePath ->
            if (imagePath.isNotEmpty()) {
                Glide.with(this)
                    .load(imagePath)
                    .override(1000, 1000)
                    .into(binding.selectedImageView)
            }
        }

        binding.fabAddImage.setOnClickListener {
            showImageSourceDialog()
        }

        binding.UpdateButton.setOnClickListener {
            updateItem()
        }
    }

    private fun initializeImageHandler() {
        imageHandler = ImageHandler(this) { bitmap ->
            binding.selectedImageView.setImageBitmap(bitmap)
        }
    }

    private fun updateDateTimeText() {
        entryDateText.text = DateTimeUtil.formatDate(selectedDateInMillis)
        entryTimeText.text = DateTimeUtil.formatTime(selectedTimeInMillis)
    }

    private fun updateItem() {
        val title = binding.updateTitle.text.toString()
        val subtitle = binding.updateSubtitle.text.toString()
        val content = binding.updateContent.text.toString()

        if (inputCheck(title, subtitle, content)) {
            val updatedEntry = Entry(args.currentEntry.id, title, subtitle, content, selectedDateInMillis, selectedTimeInMillis, args.currentEntry.imagePath)

            if (imageHandler.getSelectedImageBitmap() != null) {
                mEntryViewModel.updateEntryWithImage(updatedEntry, imageHandler.getSelectedImageBitmap()!!, requireContext()) { updatedEntryWithImage ->
                    setFragmentResult("update_request", bundleOf("is_updated" to true, "updated_entry" to updatedEntryWithImage))
                    navigateToEntryDetailFragment(updatedEntryWithImage)
                }
            } else {
                mEntryViewModel.updateEntry(updatedEntry)
                setFragmentResult("update_request", bundleOf("is_updated" to true, "updated_entry" to updatedEntry))
                navigateToEntryDetailFragment(updatedEntry)
            }

            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_LONG).show()
        }
    }

    private fun inputCheck(title: String, subtitle: String, content: String): Boolean {
        return !(TextUtils.isEmpty(title) || TextUtils.isEmpty(subtitle) || TextUtils.isEmpty(content))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.entry_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_delete) {
            deleteEntry()
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    private fun deleteEntry() {
        AlertDialog.Builder(requireContext())
            .setPositiveButton("Yes") { _, _ ->
                mEntryViewModel.deleteEntry(args.currentEntry)
                Toast.makeText(requireContext(), "Successfully Removed", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_updateFragment_to_listFragment)
            }
            .setNegativeButton("No", null)
            .setTitle("Delete ${args.currentEntry.title}?")
            .setMessage("Are You Sure You Want To Delete ${args.currentEntry.title}")
            .create()
            .show()
    }

    private fun navigateToEntryDetailFragment(updatedEntry: Entry) {
        val action = UpdateFragmentDirections.actionUpdateFragmentToEntryDetailFragment(updatedEntry, true)
        findNavController().navigate(action)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        selectedTimeInMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
        updateDateTimeText()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Capture Image", "Pick from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> imageHandler.captureImage()
                    1 -> imageHandler.pickImage()
                }
            }
            .show()
    }
}
