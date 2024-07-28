package com.example.jurnalapp.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.jurnalapp.R
import com.example.jurnalapp.databinding.CustomRowBinding
import com.example.jurnalapp.databinding.FragmentAddBinding
import com.example.jurnalapp.model.User

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var userList = emptyList<User>()

    inner class MyViewHolder(itemBinding: CustomRowBinding): RecyclerView.ViewHolder(itemBinding.root) {
        val id_txt: TextView = itemBinding.idTxt
        val firstName_txt: TextView = itemBinding.firstNameTxt
        val lastName_txt: TextView = itemBinding.lastNameTxt
        val age_txt: TextView = itemBinding.ageTxt
        val rowLayout: ConstraintLayout = itemBinding.rowLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = CustomRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
//        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_row, parent, false))
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.id_txt.text = currentItem.id.toString()
        holder.firstName_txt.text = currentItem.firstName
        holder.lastName_txt.text = currentItem.lastName
        holder.age_txt.text = currentItem.age.toString()

        holder.rowLayout.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }

//        holder.itemView.findViewById<TextView>(R.id.id_txt).text = currentItem.id.toString()
//        holder.itemView.findViewById<TextView>(R.id.firstName_txt).text = currentItem.firstName
//        holder.itemView.findViewById<TextView>(R.id.lastName_txt).text = currentItem.lastName
//        holder.itemView.findViewById<TextView>(R.id.age_txt).text = currentItem.age.toString()

//        holder.itemView.findViewById<ConstraintLayout>(R.id.rowLayout).setOnClickListener {
//            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
//            holder.itemView.findNavController().navigate(action)
//        }
    }

    fun setData(user: List<User>) {
        this.userList = user
        notifyDataSetChanged()
    }

}