package com.diparoy.lunarwhisper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.data.StarredChatroom
import com.diparoy.lunarwhisper.ui.ChatActivity
import com.google.firebase.auth.FirebaseAuth

class StarredChatroomAdapter(
    private val context: Context,
    private val starredChatrooms: ArrayList<StarredChatroom>
) : RecyclerView.Adapter<StarredChatroomAdapter.StarredChatroomViewHolder>() {

    class StarredChatroomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatPartnerNameTextView: TextView = itemView.findViewById(R.id.username)
        val chatPartnerImage: ImageView = itemView.findViewById(R.id.image)
        val latestMessageTextView: TextView = itemView.findViewById(R.id.msg)
        val timestampTextView: TextView = itemView.findViewById(R.id.msgTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StarredChatroomViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_user_row, parent, false)
        return StarredChatroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: StarredChatroomViewHolder, position: Int) {
        val chatroom = starredChatrooms[position]
        holder.chatPartnerNameTextView.text = chatroom.chatPartnerName
        holder.latestMessageTextView.text = chatroom.latestMessage
        holder.timestampTextView.text = chatroom.timestamp

        if (chatroom.chatPartnerImageUri.isNotEmpty()) {
            Glide.with(context)
                .load(chatroom.chatPartnerImageUri)
                .placeholder(R.drawable.img_4)
                .error(R.drawable.img_4)
                .into(holder.chatPartnerImage)
        } else {
            holder.chatPartnerImage.setImageResource(R.drawable.img_4)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            val chatroom = starredChatrooms[position]

            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            val recipientUid = if (currentUserUid == chatroom.senderUid) {
                chatroom.chatPartnerUid
            } else {
                chatroom.senderUid
            }

            intent.putExtra("recipientUsername", chatroom.chatPartnerName)
            intent.putExtra("userImageUri", chatroom.chatPartnerImageUri)
            intent.putExtra("recipientUid", recipientUid)

            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return starredChatrooms.size
    }
}
