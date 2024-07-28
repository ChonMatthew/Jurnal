package com.example.jurnalapp.fragments.update

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
import android.widget.TextView
import android.widget.TimePicker
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentUpdateBinding
import com.example.jurnalapp.model.Entry
import com.example.jurnalapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdateFragment : Fragment(), TimePickerDialog.OnTimeSetListener {

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedDateInMillis = 0L
    private var selectedTimeInMillis = 0L
    private lateinit var entryDateText: TextView
    private lateinit var entryTimeText: TextView

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!
    private var isEntryUpdated = false

    private val args by navArgs<UpdateFragmentArgs>()

    private lateinit var mEntryViewModel: EntryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
//        val view =  inflater.inflate(R.layout.fragment_update, container, false)

        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        selectedDateInMillis = args.currentEntry.date
        selectedTimeInMillis = args.currentEntry.time

        entryDateText = binding.entryDateText
        entryTimeText = binding.entryTimeText

        updateDateTimeText()

        binding.updateTitle.setText(args.currentEntry.title)
        binding.updateSubtitle.setText(args.currentEntry.subtitle)
        binding.updateContent.setText(args.currentEntry.content)

        binding.updateDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.updateTimeButton.setOnClickListener {
            showTimePicker()
        }

        binding.UpdateButton.setOnClickListener {
            updateItem()
        }

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDateInMillis = calendar.timeInMillis
                updateDateTimeText() // Update TextViews immediately
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
            updateDateTimeText() //Update TextViews immediately
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
//                updateDateTimeText() // Update TextViews immediately
//            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // Use 24-hour format
        )
        timePickerDialog.show()}
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        selectedTimeInMillis = calendar.timeInMillis
        updateDateTimeText()
    }

    private fun updateDateTimeText() {
        entryDateText.text = dateFormat.format(Date(selectedDateInMillis))
        entryTimeText.text = timeFormat.format(Date(selectedTimeInMillis))
    }

    private fun updateItem() {
        val title = binding.updateTitle.text.toString()
        val subtitle = binding.updateSubtitle.text.toString()
        val content = binding.updateContent.text.toString()

        if(inputCheck(title, subtitle, content)) {
            val updatedEntry = Entry(args.currentEntry.id, title, subtitle, content, selectedDateInMillis, selectedTimeInMillis)

            mEntryViewModel.updateEntry(updatedEntry)
            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_LONG).show()

            isEntryUpdated = true

            val action = UpdateFragmentDirections.actionUpdateFragmentToEntryDetailFragment(updatedEntry, true)
            findNavController().navigate(action)

//            setFragmentResult("update_request", bundleOf("updated_entry" to updatedEntry, "is_updated" to isEntryUpdated))
//            findNavController().navigateUp()
//            findNavController().navigate(R.id.action_updateFragment_to_entryDetailFragment)
        }else {
            val action = UpdateFragmentDirections.actionUpdateFragmentToEntryDetailFragment(args.currentEntry, false)
            findNavController().navigate(action)
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
}
