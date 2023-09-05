package com.example.anew.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.anew.R
import com.example.anew.model.Messages
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(
    private val context: Context,
    private var dataList: List<Messages>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {


        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val msgText : TextView = itemView.findViewById(R.id.textViewMsg)
            val usernameText: TextView = itemView.findViewById(R.id.msgUsername)
            val layoutBg : ConstraintLayout = itemView.findViewById(R.id.msgItemLayout)
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
        val data = dataList[position]
        holder.msgText.text = data.msgText
        holder.usernameText.text = data.senderUsername
        if(data.senderId == FirebaseAuth.getInstance().currentUser?.uid!!){
            val drawableResId = R.drawable.msg_sender_bg
            val drawable = ContextCompat.getDrawable(context, drawableResId)
            holder.layoutBg.background = drawable
            holder.usernameText.setTextColor(ContextCompat.getColor(context, R.color.black))
            holder.msgText.setTextColor(ContextCompat.getColor(context, R.color.black))
        }else{
            val drawableResId = R.drawable.msg_receiver_bg
            val drawable = ContextCompat.getDrawable(context, drawableResId)
            holder.layoutBg.background = drawable
            holder.usernameText.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.msgText.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    fun setData(newDataList: List<Messages>){
        dataList = newDataList
        notifyDataSetChanged()
    }


}