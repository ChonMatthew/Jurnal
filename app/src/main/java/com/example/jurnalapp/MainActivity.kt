package com.example.jurnalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.jurnalapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

//        val appBarConfiguration = AppBarConfiguration(setOf(R.id.listFragment))
        setupActionBarWithNavController(navController)

        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.listFragment -> {
                    navController.popBackStack(R.id.listFragment, false) // Pop back stack to ListFragment
                    true
                }
                R.id.searchFragment -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.searchFragment)
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }}
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