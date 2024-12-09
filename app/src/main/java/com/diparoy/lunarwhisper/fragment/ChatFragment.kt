package com.diparoy.lunarwhisper.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.adapter.ChatAdapter
import com.diparoy.lunarwhisper.data.ChatData
import com.diparoy.lunarwhisper.data.Message
import com.diparoy.lunarwhisper.data.User
import com.diparoy.lunarwhisper.ui.ChatActivity
import com.diparoy.lunarwhisper.ui.SearchActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {
    private lateinit var allChatsRecyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val chatUserList = ArrayList<ChatData>()
    private lateinit var goneLayout: LinearLayout

    private val unseenMessagesMap = mutableMapOf<String, Boolean>()
    private var openedChatPartners = mutableSetOf<String>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        goneLayout = view.findViewById(R.id.gone)

        sharedPreferences = requireContext().getSharedPreferences("ChatFragment", Context.MODE_PRIVATE)

        //Log.d("ChatFragment", "Initial openedChatPartners: $openedChatPartners")

        val openedChatPartnersSet = sharedPreferences.getStringSet("openedChatPartners", mutableSetOf())

        if (openedChatPartnersSet != null) {
            openedChatPartners = openedChatPartnersSet.toMutableSet()
            //Log.d("ChatFragment", "Retrieved openedChatPartnersSet: $openedChatPartnersSet")
        } else {
            openedChatPartners = mutableSetOf()
            //Log.d("ChatFragment", "openedChatPartnersSet is null. Creating new set.")
        }

        //Log.d("ChatFragment", "openedChatPartnersSet: $openedChatPartnersSet")
        //Log.d("ChatFragment", "openedChatPartners: $openedChatPartners")

        allChatsRecyclerView = view.findViewById(R.id.allChatsRecyclerView)
        val layoutManager = LinearLayoutManager(requireContext())
        allChatsRecyclerView.layoutManager = layoutManager

        val onItemClick: (ChatData) -> Unit = { chatData ->

            //Log.d("ChatFragment", "Before adding: openedChatPartners: $openedChatPartners")
            openedChatPartners.add(chatData.chatPartnerId)
            //Log.d("ChatFragment", "After adding: openedChatPartners: $openedChatPartners")

            val editor = sharedPreferences.edit()
            editor.putStringSet("openedChatPartners", openedChatPartners)
            editor.apply()

            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("recipientUid", chatData.chatPartnerId)
            intent.putExtra("recipientUsername", chatData.name)
            intent.putExtra("userImageUri", chatData.imageUri)
            startActivity(intent)

            chatData.unseenMessageCount = 0
            unseenMessagesMap[chatData.chatPartnerId] = false
            adapter.notifyDataSetChanged()
        }

        adapter = ChatAdapter(
            chatUserList,
            onItemClick
        )
        allChatsRecyclerView.adapter = adapter

        fun findPositionForChatPartner(chatPartnerId: String): Int {
            for (i in 0 until chatUserList.size) {
                if (chatUserList[i].chatPartnerId == chatPartnerId) {
                    return i
                }
            }
            return -1
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
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
                        if (chatUserList.none { it.chatPartnerId == chatPartnerId }) {
                            chatPartnerId?.let {
                                unseenMessagesMap[it] = true
                            }
                        }
                    }

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
                                    if (isUnseen) 1 else 0
                                )

                                unseenMessagesMap[chatPartnerId] = isUnseen
                                val position = findPositionForChatPartner(chatPartnerId)
                                if (position != -1) {
                                    adapter.updateChatData(chatData)
                                } else {
                                    adapter.addChatData(chatData)
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
                        if (chatUserList.none { it.chatPartnerId == chatPartnerId }) {
                            chatPartnerId?.let {
                                unseenMessagesMap[it] = true
                            }
                        }
                    }

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
                                    if (isUnseen) 1 else 0
                                )

                                unseenMessagesMap[chatPartnerId] = isUnseen
                                val position = findPositionForChatPartner(chatPartnerId)
                                if (position != -1) {
                                    adapter.updateChatData(chatData)
                                } else {
                                    adapter.addChatData(chatData)
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

        return view
    }
}
