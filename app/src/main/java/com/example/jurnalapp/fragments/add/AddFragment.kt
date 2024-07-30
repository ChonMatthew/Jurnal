package com.example.jurnalapp.fragments.add

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentAddBinding
import com.example.jurnalapp.model.Entry
import com.example.jurnalapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.IOException

class AddFragment : Fragment(), TimePickerDialog.OnTimeSetListener {

    // Declare variables for views and ViewModel
    private lateinit var entryDateText: TextView
    private lateinit var entryTimeText: TextView
    private lateinit var mEntryViewModel: EntryViewModel

    // Declare variables for image handling
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    // Declare date and time formatters
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val calendar = Calendar.getInstance()
    private var selectedDateInMillis = calendar.timeInMillis
    private var selectedTimeInMillis = calendar.timeInMillis

    // Declare bitmap for selected image
    private var selectedImageBitmap: Bitmap? = null

    // Declare binding variables
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        // Initialize ViewModel
        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        // Initialize views with corresponding IDs
        entryDateText = binding.entryDateText
        entryTimeText = binding.entryTimeText

        updateDateTimeText()

        // Set click listener for the DatePicker and Time Picker
        binding.pickDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.pickTimeButton.setOnClickListener {
            showTimePicker()
        }

        // Initialize image handling and activity
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    try {
                        val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                        selectedImageBitmap = ImageDecoder.decodeBitmap(source)
                        selectedImageBitmap = resizeBitmap(selectedImageBitmap!!, 5000, 5000)
                        binding.selectedImageView.setImageBitmap(selectedImageBitmap)
                    } catch (e: Exception) {
                        Log.e("AddFragment", "Error decoding image: ", e)
                    }
                }
            }
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, launch image picker
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImageLauncher.launch(intent)
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(requireContext(), "Permission denied to access images", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listener for the pick image button
        binding.pickImageButton.setOnClickListener {
            // Check for permissions and launch image picker
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted, launch image picker
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImageLauncher.launch(intent)
            } else {
                // Permission is not granted, request it
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    try {
                        val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                        selectedImageBitmap = ImageDecoder.decodeBitmap(source)
                        selectedImageBitmap = resizeBitmap(selectedImageBitmap!!, 5000, 5000)
                        binding.selectedImageView.setImageBitmap(selectedImageBitmap)
                    } catch (e: Exception) {
                        Log.e("AddFragment", "Error decoding image: ", e)
                    }
                }
            }
        }

        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                takeImage()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.captureImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takeImage()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // Set click listener for the Add button
        binding.AddButton.setOnClickListener {
            insertDataToDatabase()
        }
        return binding.root
    }

    private var latestTmpUri: Uri? = null

    private fun takeImage() {
        @Suppress("DEPRECATION")
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }}
    }

    @Throws(IOException::class)
    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile(
            "tmp_image_file",
            ".png",
            requireContext().cacheDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            tmpFile
        )
    }

    // Update the date and time text views with the formatted date and time
    private fun updateDateTimeText() {
        entryDateText.text = dateFormat.format(Date(selectedDateInMillis))
        entryTimeText.text = timeFormat.format(Date(selectedTimeInMillis))
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
        // Set a listener to check if the date has been selected from the dialog
        datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDateInMillis = calendar.timeInMillis
            updateDateTimeText() // Update TextViews immediately
        }
        datePickerDialog.show()
    }

    // Show the date picker dialog
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

    // Set a listener to check if the time has been selected from the dialog
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        selectedTimeInMillis = calendar.timeInMillis
        updateDateTimeText()
    }

    // Insert data into the database
    private fun insertDataToDatabase() {
        val title = binding.editTitle.text.toString()
        val subtitle = binding.editSubtitle.text.toString()
        val content = binding.editContent.text.toString()
        val selectedDateInMillis = calendar.timeInMillis
        val selectedTimeInMillis = calendar.timeInMillis
        val selectedImageBitmap = selectedImageBitmap

        if(inputCheck(title, subtitle, content)) {
            val entry = Entry(0, title, subtitle, content, selectedDateInMillis, selectedTimeInMillis, null)

            if (selectedImageBitmap != null) {
                mEntryViewModel.addEntryWithImage(entry, selectedImageBitmap, requireContext())
            } else {
                mEntryViewModel.addEntry(entry)
            }
            Toast.makeText(requireContext(), "Successfully Added!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_LONG).show()
        }
    }

    // Check if fields are filled
    private fun inputCheck(title: String, subtitle: String, content: String): Boolean {
        return !(title.isEmpty() || subtitle.isEmpty() || content.isEmpty())
    }

    // Method to resize the bitmap
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val bitmapRatio: Float = width.toFloat() / height.toFloat()
        return if (bitmapRatio > 1) {
            val resizedHeight = (maxWidth / bitmapRatio).toInt()
            Bitmap.createScaledBitmap(bitmap, maxWidth, resizedHeight, true)
        } else {
            val resizedWidth = (maxHeight * bitmapRatio).toInt()
            Bitmap.createScaledBitmap(bitmap, resizedWidth, maxHeight, true)
        }
    }
}
