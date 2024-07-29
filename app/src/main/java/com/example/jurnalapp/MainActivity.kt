package com.example.jurnalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.jurnalapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.listFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        val navController = findNavController(R.id.fragmentContainerView)
        when (navController.currentDestination?.id) {
            R.id.listFragment -> {
                super.onBackPressed()
            }
            R.id.addFragment -> {
                navController.navigate(R.id.action_addFragment_to_listFragment)
            }
            R.id.updateFragment -> {
                navController.navigate(R.id.action_updateFragment_to_listFragment)
            }
            R.id.entryDetailFragment -> {
                navController.navigate(R.id.action_entryDetailFragment_to_listFragment)
            }
            else -> super.onBackPressed()
        }
    }
}