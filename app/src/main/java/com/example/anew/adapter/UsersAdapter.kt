package com.example.anew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.R
import com.example.anew.model.Users

class UsersAdapter(
    private var dataList: List<Users>,
    private val onClickListenerCatchData: OnClickListenerCatchData
) :RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImg: ImageView = itemView.findViewById(R.id.circleProfileImg)
        val tvName : TextView = itemView.findViewById(R.id.tvName)
        val tvUsername : TextView = itemView.findViewById(R.id.tvUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_user_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        Glide.with(holder.itemView.context).load(data.details?.profileImg).into(holder.profileImg)
        holder.tvName.text = data.details?.name
        holder.tvUsername.text = data.username

        holder.profileImg.setOnClickListener {
            val senderId = data.userId
            senderId?.let { it ->
                onClickListenerCatchData?.onProfileImageClick(it)
            }
        }

    }

    fun setData(newDataList : List<Users>){
        dataList = newDataList
        notifyDataSetChanged()
    }
}