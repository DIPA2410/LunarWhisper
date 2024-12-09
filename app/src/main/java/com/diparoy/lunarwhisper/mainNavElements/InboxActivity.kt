package com.diparoy.lunarwhisper.mainNavElements

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.adapter.ChatAdapter
import com.diparoy.lunarwhisper.data.ChatData
import com.diparoy.lunarwhisper.data.Message
import com.diparoy.lunarwhisper.data.User
import com.diparoy.lunarwhisper.ui.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class InboxActivity : AppCompatActivity() {

    private lateinit var allMessagesRecyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val chatUserList = ArrayList<ChatData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Inbox"

        allMessagesRecyclerView = findViewById(R.id.inboxRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        allMessagesRecyclerView.layoutManager = layoutManager


        val onItemClick: (ChatData) -> Unit = { chatData ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("recipientUid", chatData.chatPartnerId)
            intent.putExtra("recipientUsername", chatData.name)
            intent.putExtra("userImageUri", chatData.imageUri)
            startActivity(intent)
        }

        adapter = ChatAdapter(
            chatUserList,
            onItemClick
        )
        allMessagesRecyclerView.adapter = adapter

        val currentUserId = FirebaseAuth.getInstance().uid
        val databaseReference = FirebaseDatabase.getInstance().getReference("/chats").orderByChild("/messages")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatUserList.clear()
                for (messageSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null && message.receiverId == currentUserId) {
                        val chatPartnerId = message.senderId
                        val userReference = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
                        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(User::class.java)
                                if (user != null) {
                                    val chatData = ChatData(
                                        chatPartnerId!!,
                                        user.name!!,
                                        user.imageUri!!,
                                        message.message!!,
                                        message.timestamp!!,
                                        0
                                    )
                                    adapter.addChatData(chatData)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}
