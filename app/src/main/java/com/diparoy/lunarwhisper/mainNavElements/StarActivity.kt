package com.diparoy.lunarwhisper.mainNavElements

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

class StarActivity : AppCompatActivity() {

    private lateinit var starredChatrooms: ArrayList<StarredChatroom>
    private lateinit var starredChatroomsAdapter: StarredChatroomAdapter
    private lateinit var starredChatroomsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_star)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Starred Threads"

        starredChatrooms = ArrayList()
        starredChatroomsAdapter = StarredChatroomAdapter(this, starredChatrooms)
        starredChatroomsRecyclerView = findViewById(R.id.starRecyclerView)
        starredChatroomsRecyclerView.layoutManager = LinearLayoutManager(this)
        starredChatroomsRecyclerView.adapter = starredChatroomsAdapter

        fetchStarredChatrooms()
    }

    private fun fetchStarredChatrooms() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val starredMessagesRef =
                FirebaseDatabase.getInstance().getReference("/starred-messages/$currentUserUid")
            starredMessagesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    starredChatrooms.clear()

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
                                            starredChatrooms.add(starredChatroom)
                                            starredChatroomsAdapter.notifyDataSetChanged()
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


/*class StarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatUserAdapter: ChatUserAdapter
    private lateinit var starredMessagesRef: DatabaseReference
    private lateinit var latestMessagesRef: DatabaseReference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_star)

        recyclerView = findViewById(R.id.starRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val onItemClick: (OtherData) -> Unit = { chatData ->
            // Handle the item click, for example, navigate to the com.diparoy.lunarwhisper.ChatActivity
            val intent = Intent(this, com.diparoy.lunarwhisper.ChatActivity::class.java)
            intent.putExtra("recipientUid", chatData.receiverId) //chatPartnerId
            intent.putExtra("recipientUsername", chatData.recipientUsername)
            intent.putExtra("userImageUri", chatData.recipientImageUri)
            startActivity(intent)
        }

        chatUserAdapter = ChatUserAdapter(this, ArrayList(), onItemClick)
        recyclerView.adapter = chatUserAdapter


        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Starred Threads"

        starredMessagesRef = FirebaseDatabase.getInstance().reference.child("starred-messages")
            .child(currentUser?.uid.toString())

        latestMessagesRef = FirebaseDatabase.getInstance().reference.child("latest-messages")
            .child(currentUser?.uid.toString())

        loadStarredChatrooms()
    }

    private fun loadStarredChatrooms() {
        val starredChatrooms = ArrayList<OtherData>()

        // Listen for changes in starred messages
        starredMessagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val receiverId = snapshot.key.toString()
                val chatroomId = snapshot.child(receiverId).value.toString()

                // Fetch data from latest-messages node
                latestMessagesRef.child(receiverId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val message = dataSnapshot.getValue(Message::class.java)
                        if (message != null) {
                            val currentUserId = FirebaseAuth.getInstance().uid
                            val chatPartnerId = if (message.senderId == currentUserId) {
                                message.receiverId
                            } else {
                                message.senderId
                            }

                            // Check if chatPartnerId is not the current user's ID
                            if (chatPartnerId != currentUserId) {
                                val recipientUsername = dataSnapshot.child("recipientUsername").value.toString()
                                val recipientImageUri = dataSnapshot.child("recipientImageUri").value.toString()
                                val messageText = dataSnapshot.child("message").value.toString()
                                val timestamp = dataSnapshot.child("timestamp").value.toString()

                                // Create OtherData object
                                val otherData = OtherData(chatPartnerId!!, recipientUsername, recipientImageUri, messageText, timestamp)
                                starredChatrooms.add(otherData)
                                chatUserAdapter.updateData(starredChatrooms)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle changes if needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle removal if needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moves if needed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
}*/

/*override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_star)

    val backIcon = findViewById<ImageView>(R.id.backIcon)
    backIcon.setOnClickListener {
        onBackPressed()
    }

    val title = findViewById<TextView>(R.id.toolbarTitle)
    title.text = "Starred Messages"

    starRecyclerView = findViewById(R.id.starRecyclerView)
    val layoutManager = LinearLayoutManager(this)
    starRecyclerView.layoutManager = layoutManager

    val onItemClick: (ChatData) -> Unit = { chatData ->
        // Handle the item click, for example, navigate to the com.diparoy.lunarwhisper.ChatActivity
        val intent = Intent(this, com.diparoy.lunarwhisper.ChatActivity::class.java)
        intent.putExtra("recipientUid", chatData.chatPartnerId)
        intent.putExtra("recipientUsername", chatData.name)
        intent.putExtra("userImageUri", chatData.imageUri)
        startActivity(intent)
    }

    chatAdapter = ChatAdapter(chatUsers, onItemClick)
    starRecyclerView.adapter = chatAdapter

    mDbRef = FirebaseDatabase.getInstance().reference

    val senderUid = FirebaseAuth.getInstance().currentUser?.uid
    val receiverUid = intent.getStringExtra("recipientUid")
    mDbRef = FirebaseDatabase.getInstance().reference

    senderRoom = receiverUid + senderUid
    receiverRoom = senderUid + receiverUid

    val starredMessagesRef = FirebaseDatabase.getInstance().getReference("/starred-messages/$senderUid/$senderRoom/latest-message")

    starredMessagesRef.addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            // Inside onChildAdded and onChildChanged callbacks
            val message = dataSnapshot.getValue(Message::class.java)
            if (message != null) {
                // Determine the chat partner's ID based on the sender and receiver IDs
                val currentUserId = FirebaseAuth.getInstance().uid
                val chatPartnerId = if (message.senderId == currentUserId) {
                    message.receiverId
                } else {
                    message.senderId
                }

                // Fetch user data for the chat partner
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
                                message.timestamp!!
                            )

                            // If the chat partner already exists in the adapter, update the data
                            val position = findPositionForChatPartner(chatPartnerId)
                            if (position != -1) {
                                chatAdapter.updateChatData(chatData)
                            } else {
                                chatAdapter.addChatData(chatData)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            // Handle changes to chat users, if necessary
            // Inside onChildAdded and onChildChanged callbacks
            val message = snapshot.getValue(Message::class.java)
            if (message != null) {
                // Determine the chat partner's ID based on the sender and receiver IDs
                val currentUserId = FirebaseAuth.getInstance().uid
                val chatPartnerId = if (message.senderId == currentUserId) {
                    message.receiverId
                } else {
                    message.senderId
                }

                // Fetch user data for the chat partner
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
                                message.timestamp!!
                            )

                            // If the chat partner already exists in the adapter, update the data
                            val position = findPositionForChatPartner(chatPartnerId)
                            if (position != -1) {
                                chatAdapter.updateChatData(chatData)
                            } else {
                                chatAdapter.addChatData(chatData)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val chatData = dataSnapshot.getValue(ChatData::class.java)
            if (chatData != null) {
                chatAdapter.removeChatData(chatData)
            }
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            // Handle movement of chat users, if necessary
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Handle errors
        }
    })
}

private fun findPositionForChatPartner(chatPartnerId: String): Int {
    for (i in 0 until chatUsers.size) {
        if (chatUsers[i].chatPartnerId == chatPartnerId) {
            return i // Return the position of the chat partner in the list
        }
    }
    return -1 // Return -1 if the chat partner is not found in the list
}
}*/
