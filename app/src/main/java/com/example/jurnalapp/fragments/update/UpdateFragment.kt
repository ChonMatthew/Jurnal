package com.example.jurnalapp.fragments.update

import android.app.Activity
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
import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentUpdateBinding
import com.example.jurnalapp.model.Entry
import com.example.jurnalapp.viewmodel.EntryViewModel
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdateFragment : Fragment(), TimePickerDialog.OnTimeSetListener {

    // Declare variables for views and ViewModel
    private lateinit var entryDateText: TextView
    private lateinit var entryTimeText: TextView
    private lateinit var mEntryViewModel: EntryViewModel

    // Declare variables for image handling
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    // Declare bitmap for selected image
    private var selectedImageBitmap: Bitmap? = null

    // Declare date and time formatters
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedDateInMillis = 0L
    private var selectedTimeInMillis = 0L

    // Declare binding variables
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    // Declare flag to check if entry is updated
    private var isEntryUpdated = false

    // Get the arguments passed from the ListFragment
    private val args by navArgs<UpdateFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        // Initialize ViewModel
        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        // Initialize views with corresponding IDs
        binding.updateTitle.setText(args.currentEntry.title)
        binding.updateSubtitle.setText(args.currentEntry.subtitle)
        binding.updateContent.setText(args.currentEntry.content)

        // Set the selected date and time in the text views
        selectedDateInMillis = args.currentEntry.date
        selectedTimeInMillis = args.currentEntry.time
        entryDateText = binding.entryDateText
        entryTimeText = binding.entryTimeText

        updateDateTimeText()

        // Set click listeners for the date and time buttons
        binding.updateDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.updateTimeButton.setOnClickListener {
            showTimePicker()
        }

        args.currentEntry.imagePath?.let { imagePath ->
            if (imagePath.isNotEmpty()) {
                Glide.with(this)
                    .load(imagePath)
                    .override(250, 250)
                    .into(binding.selectedImageView)
            }
        }

        // Initialize image handling and activity
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    try {
                        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeStream(inputStream, null, options)

                        val imageHeight = options.outHeight
                        val imageWidth = options.outWidth
                        val reqHeight = 2000 // Desired height
                        val reqWidth = 2000 // Desired width

                        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                        options.inJustDecodeBounds = false

                        val scaledBitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri), null, options)

                        selectedImageBitmap = scaledBitmap
                        binding.selectedImageView.setImageBitmap(scaledBitmap)
                    } catch (e: Exception) {
                        Log.e("UpdateFragment", "Error decoding image: ", e)
                    }
                }
            }
        }

        binding.pickImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Set click listener for the update button
        binding.UpdateButton.setOnClickListener {
            updateItem()
        }

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        return binding.root
    }

    // Show the date picker dialog
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDateInMillis = calendar.timeInMillis
                updateDateTimeText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDateInMillis = calendar.timeInMillis
            updateDateTimeText()
        }
        datePickerDialog.show()
    }

    // Show the time picker dialog
    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            this,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        selectedTimeInMillis = calendar.timeInMillis
        updateDateTimeText()
    }

    // Update the date and time text views with the formatted date and time
    private fun updateDateTimeText() {
        entryDateText.text = dateFormat.format(Date(selectedDateInMillis))
        entryTimeText.text = timeFormat.format(Date(selectedTimeInMillis))
    }

    // Update the entry in the database
    private fun updateItem() {
        val title = binding.updateTitle.text.toString()
        val subtitle = binding.updateSubtitle.text.toString()
        val content = binding.updateContent.text.toString()

        if(inputCheck(title, subtitle, content)) {
            val updatedEntry = Entry(args.currentEntry.id, title, subtitle, content, selectedDateInMillis, selectedTimeInMillis, null)
            isEntryUpdated = true

            if (selectedImageBitmap != null) {
                mEntryViewModel.updateEntryWithImage(args.currentEntry, selectedImageBitmap!!, requireContext())
                { updatedEntryWithImage ->
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
        if(item.itemId == R.id.menu_delete){
            deleteEntry()
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
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete ${args.currentEntry.title}?")
        builder.setMessage("Are You Sure You Want To Delete ${args.currentEntry.title}")
        builder.create().show()
    }

    private fun navigateToEntryDetailFragment(updatedEntry: Entry) {
        val action = UpdateFragmentDirections.actionUpdateFragmentToEntryDetailFragment(updatedEntry, true)
        findNavController().navigate(action)
    }

    // Calculate inSampleSize value for BitmapFactory.Options to downsample the image
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
