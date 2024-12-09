package com.diparoy.lunarwhisper.mainNavElements

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PromotionalActivity : AppCompatActivity() {
    private lateinit var promoRecyclerView: RecyclerView
    private lateinit var promoAdapter: ChatAdapter
    private val promoChatList = ArrayList<ChatData>()

    private val unseenMessagesMap = mutableMapOf<String, Boolean>()
    private var openedChatPartners = mutableSetOf<String>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promotional)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Promotional"

        sharedPreferences = getSharedPreferences("Promotional", Context.MODE_PRIVATE)

        val openedChatPartnersSet = sharedPreferences.getStringSet("openedChatPartners", mutableSetOf())

        if (openedChatPartnersSet != null) {
            openedChatPartners = openedChatPartnersSet.toMutableSet()
        } else {
            openedChatPartners = mutableSetOf()
        }

        promoRecyclerView = findViewById(R.id.promoRecyclerView)
        promoRecyclerView.layoutManager = LinearLayoutManager(this)

        val onItemClick: (ChatData) -> Unit = { chatData ->

            openedChatPartners.add(chatData.chatPartnerId)

            val editor = sharedPreferences.edit()
            editor.putStringSet("openedChatPartners", openedChatPartners)
            editor.apply()

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("recipientUid", chatData.chatPartnerId)
            intent.putExtra("recipientUsername", chatData.name)
            intent.putExtra("userImageUri", chatData.imageUri)
            startActivity(intent)

            chatData.unseenMessageCount = 0
            unseenMessagesMap[chatData.chatPartnerId] = false
            promoAdapter.notifyDataSetChanged()
        }

        promoAdapter = ChatAdapter(
            promoChatList,
            onItemClick
        )
        promoRecyclerView.adapter = promoAdapter

        fun findPositionForChatPartner(chatPartnerId: String): Int {
            for (i in 0 until promoChatList.size) {
                if (promoChatList[i].chatPartnerId == chatPartnerId) {
                    return i
                }
            }
            return -1
        }

        val currentUserId = FirebaseAuth.getInstance().uid
        val databaseReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$currentUserId")

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val message = dataSnapshot.getValue(Message::class.java)
                if (message != null) {
                    val currentUserId = FirebaseAuth.getInstance().uid
                    val chatPartnerId = if (message.senderId == currentUserId) {
                        message.receiverId
                    } else {
                        message.senderId
                    }

                    val isUnseen = if (message.senderId != currentUserId) {
                        unseenMessagesMap[chatPartnerId] ?: true
                    } else {
                        false
                    }

                    if (openedChatPartners.contains(chatPartnerId)) {
                        chatPartnerId?.let {
                            unseenMessagesMap[it] = false
                        }
                    } else {
                        if (promoChatList.none { it.chatPartnerId == chatPartnerId }) {
                            chatPartnerId?.let {
                                unseenMessagesMap[it] = true
                            }
                        }
                    }

                    val userReference = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
                    userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val user = userSnapshot.getValue(User::class.java)
                            if (user != null && user.userType == "Promotional User") {
                                val chatData = ChatData(
                                    chatPartnerId!!,
                                    user.name!!,
                                    user.imageUri!!,
                                    message.message!!,
                                    message.timestamp!!,
                                    if (isUnseen) 1 else 0
                                )

                                val position = findPositionForChatPartner(chatPartnerId)
                                if (position != -1) {
                                    promoAdapter.updateChatData(chatData)
                                } else {
                                    promoAdapter.addChatData(chatData)
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    val currentUserId = FirebaseAuth.getInstance().uid
                    val chatPartnerId = if (message.senderId == currentUserId) {
                        message.receiverId
                    } else {
                        message.senderId
                    }

                    val isUnseen = if (message.senderId != currentUserId) {
                        unseenMessagesMap[chatPartnerId] ?: true
                    } else {
                        false
                    }

                    if (openedChatPartners.contains(chatPartnerId)) {
                        chatPartnerId?.let {
                            unseenMessagesMap[it] = false
                        }
                    } else {
                        if (promoChatList.none { it.chatPartnerId == chatPartnerId }) {
                            chatPartnerId?.let {
                                unseenMessagesMap[it] = true
                            }
                        }
                    }

                    val userReference = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
                    userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val user = userSnapshot.getValue(User::class.java)
                            if (user != null && user.userType == "Promotional User") {
                                val chatData = ChatData(
                                    chatPartnerId!!,
                                    user.name!!,
                                    user.imageUri!!,
                                    message.message!!,
                                    message.timestamp!!,
                                    if (isUnseen) 1 else 0
                                )

                                val position = findPositionForChatPartner(chatPartnerId)
                                if (position != -1) {
                                    promoAdapter.updateChatData(chatData)
                                } else {
                                    promoAdapter.addChatData(chatData)
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}
