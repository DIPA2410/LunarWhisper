package com.diparoy.lunarwhisper.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.data.ChatData

class ChatAdapter(private val chatUsers: MutableList<ChatData>, private val onItemClick: (ChatData) -> Unit) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val usernameTextView: TextView = itemView.findViewById(R.id.username)
        val userImageView: ImageView = itemView.findViewById(R.id.image)
        val msg: TextView = itemView.findViewById(R.id.msg)
        val msgTime: TextView = itemView.findViewById(R.id.msgTime)
        val indicatorUnseen: View = itemView.findViewById(R.id.indicatorUnseen)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(chatUsers[position])

                    chatUsers[position].unseenMessageCount = 0
                    indicatorUnseen.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_user_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatUsers.size
    }

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        val chatUser = chatUsers[position]
        holder.usernameTextView.text = chatUser.name
        holder.msg.text = chatUser.message
        holder.msgTime.text = chatUser.timestamp

        Glide.with(holder.itemView)
            .load(chatUser.imageUri)
            .into(holder.userImageView)

        holder.indicatorUnseen.visibility = if (chatUser.unseenMessageCount > 0) View.VISIBLE else View.GONE

    }

    fun addChatData(chatData: ChatData) {
        chatUsers.add(chatData)
        chatUsers.sortByDescending { it.timestamp }
        notifyDataSetChanged()
    }

    fun updateChatData(chatData: ChatData) {
        val position = chatUsers.indexOfFirst { it.chatPartnerId == chatData.chatPartnerId }
        if (position != -1) {
            chatUsers[position] = chatData
            chatUsers.sortByDescending { it.timestamp }
            notifyDataSetChanged()
        }
    }

    fun updateChatUsers(updatedChatUsers: List<ChatData>) {
        chatUsers.clear()
        chatUsers.addAll(updatedChatUsers)
        notifyDataSetChanged()
    }

    fun removeChatData(chatData: ChatData) {
        val position = chatUsers.indexOf(chatData)
        if (position != -1) {
            chatUsers.removeAt(position)
            notifyItemRemoved(position)
        }
    }

}
