package com.diparoy.lunarwhisper.mainNavElements

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.adapter.StarredChatroomAdapter
import com.diparoy.lunarwhisper.data.StarredChatroom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SpamActivity : AppCompatActivity() {
    private lateinit var spamChatrooms: ArrayList<StarredChatroom>
    private lateinit var spamChatroomsAdapter: StarredChatroomAdapter
    private lateinit var spamChatroomsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spam)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Spam"

        spamChatrooms = ArrayList()
        spamChatroomsAdapter = StarredChatroomAdapter(this, spamChatrooms)
        spamChatroomsRecyclerView = findViewById(R.id.spamRecyclerView)
        spamChatroomsRecyclerView.layoutManager = LinearLayoutManager(this)
        spamChatroomsRecyclerView.adapter = spamChatroomsAdapter

        fetchStarredChatrooms()
    }

    private fun fetchStarredChatrooms() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val starredMessagesRef =
                FirebaseDatabase.getInstance().getReference("/spam-messages/$currentUserUid")
            starredMessagesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    spamChatrooms.clear()

                    for (child in dataSnapshot.children) {
                        val receiverUid = child.key
                        if (receiverUid != null) {
                            val chatroomRef = FirebaseDatabase.getInstance()
                                .getReference("/latest-messages/$currentUserUid/$receiverUid")

                            chatroomRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(chatroomSnapshot: DataSnapshot) {
                                    val senderId =
                                        chatroomSnapshot.child("senderId").value.toString()
                                    val receiverId =
                                        chatroomSnapshot.child("receiverId").value.toString()
                                    val chatPartnerUid =
                                        if (senderId == currentUserUid) receiverId else senderId

                                    val userRef = FirebaseDatabase.getInstance()
                                        .getReference("/users/$chatPartnerUid")
                                    userRef.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            val chatPartnerName =
                                                userSnapshot.child("name").value.toString()
                                            val chatPartnerImageUri =
                                                userSnapshot.child("imageUri").value.toString()
                                            val latestMessage =
                                                chatroomSnapshot.child("message").value.toString()
                                            val timestamp =
                                                chatroomSnapshot.child("timestamp").value.toString()

                                            val starredChatroom = StarredChatroom(
                                                currentUserUid,
                                                chatPartnerUid,
                                                chatPartnerName,
                                                chatPartnerImageUri,
                                                latestMessage,
                                                timestamp
                                            )
                                            spamChatrooms.add(starredChatroom)
                                            spamChatroomsAdapter.notifyDataSetChanged()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }
                                    })
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}
