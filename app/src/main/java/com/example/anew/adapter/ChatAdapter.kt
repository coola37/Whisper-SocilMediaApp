package com.example.anew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.anew.R
import com.example.anew.model.Messages

class ChatAdapter(
    private var dataList: List<Messages>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val msgText : TextView = itemView.findViewById(R.id.textViewMsg)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.msg_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    fun setData(newDataList: List<Messages>){
        dataList = newDataList
        notifyDataSetChanged()
    }


}