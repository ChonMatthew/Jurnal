package com.example.jurnalapp.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentListBinding
import com.example.jurnalapp.databinding.FragmentUpdateBinding
import com.example.jurnalapp.model.User
import com.example.jurnalapp.viewmodel.UserViewModel

class UpdateFragment : Fragment() {

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<UpdateFragmentArgs>()

    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
//        val view =  inflater.inflate(R.layout.fragment_update, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.updateFirstName.setText(args.currentUser.firstName)
        binding.updateLastName.setText(args.currentUser.lastName)
        binding.updateAge.setText(args.currentUser.age.toString())

        binding.UpdateButton.setOnClickListener {
            updateItem()
        }

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun updateItem() {
        val firstName = binding.updateFirstName.text.toString()
        val lastName = binding.updateLastName.text.toString()
        val age = Integer.parseInt(binding.updateAge.text.toString())
        val input_age = binding.updateAge.text

        if(inputCheck(firstName, lastName, input_age)) {
            val updatedUser = User(args.currentUser.id, firstName, lastName, age)

            mUserViewModel.updateUser(updatedUser)
            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_LONG).show()
        }
    }

    private fun inputCheck(firstName: String, lastName: String, age: Editable): Boolean {
        return !(TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || age.isEmpty())

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete){
            deleteUser()
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    private fun deleteUser() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_,_ ->
            mUserViewModel.deleteUser(args.currentUser)
            Toast.makeText(requireContext(), "Successfully Removed", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete ${args.currentUser.firstName}?")
        builder.setMessage("Are You Sure You Want To Delete ${args.currentUser.firstName}")
        builder.create().show()
    }
}
