package com.example.jurnalapp.fragments.add

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.FragmentAddBinding
import com.example.jurnalapp.databinding.FragmentListBinding
import com.example.jurnalapp.model.User
import com.example.jurnalapp.viewmodel.UserViewModel

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.AddButton.setOnClickListener {
            insertDataToDatabase()
        }

        return binding.root
    }

    private fun insertDataToDatabase() {
        val firstName = binding.editFirstName.text.toString()
        val lastName = binding.editLastName.text.toString()
        val age = binding.editAge.text

//        val firstName = view?.findViewById<EditText>(R.id.editFirstName)?.text.toString()
//        val lastName = view?.findViewById<EditText>(R.id.editLastName)?.text.toString()
//        val age = requireView().findViewById<EditText>(R.id.editAge).text

        if(inputCheck(firstName, lastName, age)) {
            val user = User(0, firstName, lastName, Integer.parseInt(age.toString()))

            mUserViewModel.addUser(user)
            Toast.makeText(requireContext(), "Successfully Added!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_LONG).show()
        }
    }

    private fun inputCheck(firstName: String, lastName: String, age: Editable): Boolean {
        return !(firstName.isEmpty() || lastName.isEmpty() || age.isEmpty())

    }
}