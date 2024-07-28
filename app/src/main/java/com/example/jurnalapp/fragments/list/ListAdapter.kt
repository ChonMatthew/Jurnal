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
import com.example.jurnalapp.model.Entry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var entryList = emptyList<Entry>()

    inner class MyViewHolder(itemBinding: CustomRowBinding): RecyclerView.ViewHolder(itemBinding.root) {
        val title_txt: TextView = itemBinding.titleTxt
        val subtitle_txt: TextView = itemBinding.subtitleTxt
        val content_txt: TextView = itemBinding.contentTxt
        val selectedDateText: TextView = itemBinding.selectedDateText
        val selectedTimeText: TextView = itemBinding.selectedTimeText
        val rowLayout: ConstraintLayout = itemBinding.rowLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = CustomRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return entryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = entryList[position]
        holder.title_txt.text = currentItem.title
        holder.subtitle_txt.text = currentItem.subtitle
        holder.content_txt.text = currentItem.content

        // Format date and time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val date = Date(currentItem.date)
        val time = Date(currentItem.time)
        holder.selectedDateText.text = dateFormat.format(date)
        holder.selectedTimeText.text = timeFormat.format(time)

        holder.rowLayout.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToEntryDetailFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(entry: List<Entry>) {
        this.entryList = entry
        notifyDataSetChanged()
    }

}