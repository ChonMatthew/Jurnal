package com.example.jurnalapp.fragments.add

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
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
import com.example.jurnalapp.data.handler.ImageHandler
import com.example.jurnalapp.data.util.DateTimeUtil
import com.example.jurnalapp.data.viewmodel.AddEntryViewModel
import com.example.jurnalapp.databinding.FragmentAddBinding
import java.util.Calendar

class AddFragment : Fragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var entryDateText: TextView
    private lateinit var entryTimeText: TextView
    private lateinit var addEntryViewModel: AddEntryViewModel
    private lateinit var imageHandler: ImageHandler

    private var selectedDateInMillis = Calendar.getInstance().timeInMillis
    private var selectedTimeInMillis = Calendar.getInstance().timeInMillis

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        addEntryViewModel = ViewModelProvider(this).get(AddEntryViewModel::class.java)

        setupUI()
        initializeImageHandler()

        return binding.root
    }
    private fun setupUI() {
        entryDateText = binding.entryDateText
        entryTimeText = binding.entryTimeText

        updateDateTimeText()

        binding.pickDateButton.setOnClickListener {
            DateTimeUtil.showDatePicker(requireContext()) { dateInMillis ->
                selectedDateInMillis = dateInMillis
                updateDateTimeText()
            }
        }

        binding.pickTimeButton.setOnClickListener {
            DateTimeUtil.showTimePicker(requireContext()) { timeInMillis ->
                selectedTimeInMillis = timeInMillis
                updateDateTimeText()
            }
        }

        binding.fabAddImage.setOnClickListener {
            showImageSourceDialog()
        }

        binding.AddButton.setOnClickListener {
            insertDataToDatabase()
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

    private fun insertDataToDatabase() {
        val title = binding.editTitle.text.toString()
        val subtitle = binding.editSubtitle.text.toString()
        val content = binding.editContent.text.toString()
        val selectedImageBitmap = imageHandler.getSelectedImageBitmap()

        addEntryViewModel.insertDataToDatabase(
            title,
            subtitle,
            content,
            selectedDateInMillis,
            selectedTimeInMillis,
            selectedImageBitmap,
            requireContext()
        ) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Successfully Added!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_addFragment_to_listFragment)
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_LONG).show()
            }
        }
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
