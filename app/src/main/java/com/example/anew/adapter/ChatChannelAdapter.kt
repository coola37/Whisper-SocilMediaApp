package com.example.anew.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.R
import com.example.anew.model.ChatChannel

class ChatChannelAdapter(
    private var dataList: List<ChatChannel>,
    private val onClickListenerCatchDataReceiverId: OnClickListenerCatchData) : RecyclerView.Adapter<ChatChannelAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val lastMsg : TextView = itemView.findViewById(R.id.textViewLastMsg)
            val receiverUsername: TextView = itemView.findViewById(R.id.textViewSenderUsername)
            val receiverProfileImg: ImageView = itemView.findViewById(R.id.msgSenderProfile)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_channel_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        holder.lastMsg.text = data.lastMessage
        holder.receiverUsername.text = data.receiverUsername
        holder.itemView.setOnClickListener {
            val receiverId = data.recevierId
            onClickListenerCatchDataReceiverId.onProfileImageClick(receiverId!!)
        }

        Glide.with(holder.itemView.context).load(data.receiverProfileImg).into(holder.receiverProfileImg)
    }

    fun setData(newDataList: List<ChatChannel>){
        dataList = newDataList
        notifyDataSetChanged()
    }
}