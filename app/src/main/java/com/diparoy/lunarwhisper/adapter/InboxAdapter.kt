package com.diparoy.lunarwhisper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.data.InboxItem

class InboxAdapter(var messages: List<InboxItem>) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderName: TextView = itemView.findViewById(R.id.username)
        val messageText: TextView = itemView.findViewById(R.id.msg)
        val timestamp: TextView = itemView.findViewById(R.id.msgTime)
        val senderImage: ImageView = itemView.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_user_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.senderName.text = message.senderName
        holder.messageText.text = message.message
        holder.timestamp.text = message.timestamp

        Glide.with(holder.itemView.context)
            .load(message.senderImageUri)
            .into(holder.senderImage)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}