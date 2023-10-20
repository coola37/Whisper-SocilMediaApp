package com.example.anew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.R
import com.example.anew.model.Comments
import com.example.anew.model.Notifications

class NotificationAdapter(
    private var dataList: List<Notifications>,
    private var onClickListenerCatchData: OnClickListenerCatchData
): RecyclerView.Adapter<NotificationAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val username: TextView = itemView.findViewById(R.id.notificationSenderUsername)
        val body: TextView = itemView.findViewById(R.id.notificationBodyText)
        val profileImg: ImageView = itemView.findViewById(R.id.notificationProfileImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): NotificationAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        val data = dataList[position]

        Glide.with(holder.itemView.context).load(data.senderProfileImg).into(holder.profileImg)
        holder.body.text = data.body
        holder.username.text = data.senderUsername

        holder.itemView.setOnClickListener {
            onClickListenerCatchData.onProfileImageClick(data.postId!!)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(newDataList: List<Notifications>){
        dataList = newDataList
        notifyDataSetChanged()
    }
}