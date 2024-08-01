package com.example.jurnalapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.jurnalapp.data.viewmodel.EntryViewModel
import com.example.jurnalapp.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val entryViewModel: EntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        applyTheme() // Apply the theme before setting the content view

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar as the action bar
        val toolbar: Toolbar = binding.myToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back button
        supportActionBar?.title = "Jurnal Settings" // Set the toolbar title

        setupThemeToggleButtons()

        binding.deleteAllEntriesButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupThemeToggleButtons() {
        val toggleButtons = listOf(binding.themeGreen, binding.themePurple, binding.themeBlue)

        val theme = sharedPreferences.getString("theme", "green")
        when (theme) {
            "green" -> binding.themeGreen.isChecked = true
            "purple" -> binding.themePurple.isChecked = true
            "blue" -> binding.themeBlue.isChecked = true
        }

        for (button in toggleButtons) {
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    toggleButtons.filter { it != button }.forEach { it.isChecked = false }
                    val editor = sharedPreferences.edit()
                    when (button.id) {
                        R.id.themeGreen -> editor.putString("theme", "green")
                        R.id.themePurple -> editor.putString("theme", "purple")
                        R.id.themeBlue -> editor.putString("theme", "blue")
                    }
                    editor.apply()

                    // Notify MainActivity to restart with the new theme
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    // Restart the SettingsActivity to apply the new theme
                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(settingsIntent)
                    finish()
                }
            }
        }
    }

    private fun applyTheme() {
        val theme = sharedPreferences.getString("theme", "green")
        when (theme) {
            "green" -> setTheme(R.style.Theme_JurnalApp)
            "purple" -> setTheme(R.style.Theme_JurnalApp_2)
            "blue" -> setTheme(R.style.Theme_JurnalApp_3)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete all journal entries? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                entryViewModel.deleteAllEntries()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Handle back button click
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                @Suppress("DEPRECATION")
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
