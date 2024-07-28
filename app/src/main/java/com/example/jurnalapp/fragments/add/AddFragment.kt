package com.example.jurnalapp.fragments.add

import android.app.DatePickerDialog
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
import com.example.jurnalapp.databinding.FragmentAddBinding
import com.example.jurnalapp.model.Entry
import com.example.jurnalapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddFragment : Fragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var entryDateText: TextView
    private lateinit var entryTimeText: TextView
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val calendar = Calendar.getInstance()
    private var selectedDateInMillis = calendar.timeInMillis
    private var selectedTimeInMillis = calendar.timeInMillis

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var mEntryViewModel: EntryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        entryDateText = binding.entryDateText
        entryTimeText = binding.entryTimeText

        updateDateTimeText()

        binding.pickDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.pickTimeButton.setOnClickListener {
            showTimePicker()
        }

        binding.AddButton.setOnClickListener {
            insertDataToDatabase()
        }

        return binding.root
    }
    private fun updateDateTimeText() {
        entryDateText.text = dateFormat.format(Date(selectedDateInMillis))
        entryTimeText.text = timeFormat.format(Date(selectedTimeInMillis))
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDateInMillis = calendar.timeInMillis
                updateDateTimeText()
                // Update the date text view (if you have one)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            // This listener is triggered when the user sets a date
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDateInMillis = calendar.timeInMillis
            updateDateTimeText() // Update TextViews immediately
        }
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            this,
//            { _, hourOfDay, minute ->
//                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
//                calendar.set(Calendar.MINUTE, minute)
//                selectedTimeInMillis = calendar.timeInMillis
//                updateDateTimeText()
//                // Update the time text view (if you have one)
//            }
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // Use 24-hour format
        )
        timePickerDialog.show()
    }
        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            selectedTimeInMillis = calendar.timeInMillis
            updateDateTimeText()
        }


    private fun insertDataToDatabase() {
        val title = binding.editTitle.text.toString()
        val subtitle = binding.editSubtitle.text.toString()
        val content = binding.editContent.text.toString()

        if(inputCheck(title, subtitle, content)) {
            val entry = Entry(0, title, subtitle, content, selectedDateInMillis, selectedTimeInMillis)

            mEntryViewModel.addEntry(entry)
            Toast.makeText(requireContext(), "Successfully Added!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_LONG).show()
        }
    }

    private fun inputCheck(title: String, subtitle: String, content: String): Boolean {
        return !(title.isEmpty() || subtitle.isEmpty() || content.isEmpty())

    }
}