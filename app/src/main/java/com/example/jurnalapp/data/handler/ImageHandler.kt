package com.example.jurnalapp.data.handler

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageHandler(
    private val fragment: Fragment,
    private val imageSelectedCallback: (Bitmap) -> Unit
) {

    private var pickImageLauncher: ActivityResultLauncher<Intent>
    private var requestPermissionLauncher: ActivityResultLauncher<String>
    private var takePictureLauncher: ActivityResultLauncher<Uri>
    private var selectedImageBitmap: Bitmap? = null
    private var latestTmpUri: Uri? = null

    init {
        // Initialize image pickers and permission launchers
        pickImageLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val selectedImageUri = result.data?.data
                    selectedImageUri?.let { uri ->
                        fragment.lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val source = ImageDecoder.createSource(
                                    fragment.requireContext().contentResolver,
                                    uri
                                )
                                selectedImageBitmap = ImageDecoder.decodeBitmap(source)
                                selectedImageBitmap =
                                    resizeBitmap(selectedImageBitmap!!, 1000, 1000)
                                withContext(Dispatchers.Main) {
                                    imageSelectedCallback(selectedImageBitmap!!)
                                }
                            } catch (e: Exception) {
                                Log.e("ImageHandler", "Error decoding image: ", e)
                            }
                        }
                    }
                }
            }

        requestPermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, launch image picker
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImageLauncher.launch(intent)
            } else {
                // Permission denied
                Toast.makeText(
                    fragment.requireContext(),
                    "Permission denied to access images",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        takePictureLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    latestTmpUri?.let { uri ->
                        try {
                            val source = ImageDecoder.createSource(
                                fragment.requireContext().contentResolver,
                                uri
                            )
                            selectedImageBitmap = ImageDecoder.decodeBitmap(source)
                            selectedImageBitmap = resizeBitmap(selectedImageBitmap!!, 1000, 1000)
                            imageSelectedCallback(selectedImageBitmap!!)
                        } catch (e: Exception) {
                            Log.e("ImageHandler", "Error decoding image: ", e)
                        }
                    }
                }
            }
    }

    fun pickImage() {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    fun captureImage() {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takeImage()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestCameraPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takeImage()
        } else {
            Toast.makeText(
                fragment.requireContext(),
                "Permission denied to use camera",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun takeImage() {
        @Suppress("DEPRECATION")
        fragment.lifecycleScope.launchWhenStarted {
            getTmpFileUri()?.let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri? {
        val tmpFile = File.createTempFile(
            "tmp_image_file",
            ".png",
            fragment.requireContext().cacheDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            fragment.requireContext(),
            "${fragment.requireContext().packageName}.provider",
            tmpFile
        )
    }

    fun getSelectedImageBitmap(): Bitmap? {
        return selectedImageBitmap
    }

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


