package com.example.anew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.R
import com.example.anew.model.Posts

class HomePostsAdapter(
    private var dataList: List<Posts>,
    private val onClickListenerCatchData: OnClickListenerCatchData,
    private val onLikeButtonClickListener: OnClickListenerCatchData,
    private val onTextClickListener: OnClickListenerCatchData

) : RecyclerView.Adapter<HomePostsAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfil: ImageView = itemView.findViewById(R.id.imagePostProfil)
        val imageViewPostimg: ImageView = itemView.findViewById(R.id.imageViewPostImg)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewUsername: TextView = itemView.findViewById(R.id.textViewUsername)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        val textViewPostText: TextView = itemView.findViewById(R.id.textViewPostText)
        val textViewLikeCount: TextView = itemView.findViewById(R.id.textViewLikeCount)
        val imageViewLike: ImageView = itemView.findViewById(R.id.imageViewLike)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_item, parent, false)


        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        Glide.with(holder.itemView.context).load(data.senderImg).into(holder.imageProfil)
        Glide.with(holder.itemView.context).load(data.postImg).into(holder.imageViewPostimg)
        holder.textViewName.text = data.senderName
        holder.textViewUsername.text = data.senderUsername
        holder.textViewDate.text = data.date
        holder.textViewPostText.text = data.text
        holder.textViewLikeCount.text = data.like.toString()

        holder.imageProfil.setOnClickListener {
            val senderId = data.senderID
            onClickListenerCatchData.onProfileImageClick(senderId!!)

        }
        holder.imageViewLike.setOnClickListener {
            val postId = data.postID
            onLikeButtonClickListener.onProfileImageClick(postId!!)
        }
        holder.textViewPostText.setOnClickListener {
            val postId = data.postID
            onTextClickListener.onProfileImageClick(postId!!)
        }

    }

    fun setData(newDataList: List<Posts>) {
        dataList = newDataList
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

}