package com.example.jurnalapp.fragments.update

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentUpdateBinding
import com.example.jurnalapp.model.Entry
import com.example.jurnalapp.viewmodel.EntryViewModel
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class UpdateFragment : Fragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var entryDateText: TextView
    private lateinit var entryTimeText: TextView
    private lateinit var mEntryViewModel: EntryViewModel

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    private var selectedImageBitmap: Bitmap? = null

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedDateInMillis = 0L
    private var selectedTimeInMillis = 0L

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private var isEntryUpdated = false
    private var latestTmpUri: Uri? = null

    private val args by navArgs<UpdateFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        mEntryViewModel = ViewModelProvider(this).get(EntryViewModel::class.java)

        binding.updateTitle.setText(args.currentEntry.title)
        binding.updateSubtitle.setText(args.currentEntry.subtitle)
        binding.updateContent.setText(args.currentEntry.content)

        selectedDateInMillis = args.currentEntry.date
        selectedTimeInMillis = args.currentEntry.time
        entryDateText = binding.entryDateText
        entryTimeText = binding.entryTimeText

        updateDateTimeText()

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

                        val reqHeight = 5000
                        val reqWidth = 5000

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

        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                takeImage()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
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
                        Log.e("UpdateFragment", "Error decoding image: ", e)
                    }
                }
            }
        }

        binding.captureImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takeImage()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.pickImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        binding.UpdateButton.setOnClickListener {
            updateItem()
        }

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun takeImage() {
        @Suppress("DEPRECATION")
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }
        }
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
        datePickerDialog.show()
    }

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

    private fun updateDateTimeText() {
        entryDateText.text = dateFormat.format(Date(selectedDateInMillis))
        entryTimeText.text = timeFormat.format(Date(selectedTimeInMillis))
    }

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

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val aspectRatio: Float = bitmap.width.toFloat() / bitmap.height.toFloat()
        var scaledWidth = width
        var scaledHeight = height

        if (width / aspectRatio <= height) {
            scaledHeight = (width / aspectRatio).toInt()
        } else {
            scaledWidth = (height * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
}
