package com.example.anew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.R
import com.example.anew.model.Comments

class CommentsAdapter(
    private var dataList: List<Comments>,
    private val onClickListenerCatchDataImg: OnClickListenerCatchData,
    private val onClickListenerCatchDataLike: OnClickListenerCatchData
): RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val commentsProfileImg : ImageView = itemView.findViewById(R.id.commentsProfileImg)
        val imageViewLikeComments : ImageView = itemView.findViewById(R.id.imageViewLikeComments)

        val textViewCommentsUsername : TextView = itemView.findViewById(R.id.textViewCommentsUsername)
        val commentText : TextView = itemView.findViewById(R.id.commentText)
        val textViewLikeCountComments : TextView = itemView.findViewById(R.id.textViewLikeCountComments)


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.comments_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val data = dataList[position]

       Glide.with(holder.itemView.context).load(data.senderProfileImg).into(holder.commentsProfileImg)
       holder.commentText.text = data.commentsText
       holder.textViewCommentsUsername.text = data.senderUsername
       holder.textViewLikeCountComments.text = data.likeCounts.toString()

        holder.commentsProfileImg.setOnClickListener {
            val senderId = data.senderID
            onClickListenerCatchDataImg.onProfileImageClick(senderId!!)
        }
        holder.imageViewLikeComments.setOnClickListener {
            val commentsId = data.commentsID
            onClickListenerCatchDataLike.onProfileImageClick(commentsId!!)
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(newDataList: List<Comments>){
        dataList = newDataList
        notifyDataSetChanged()
    }

}