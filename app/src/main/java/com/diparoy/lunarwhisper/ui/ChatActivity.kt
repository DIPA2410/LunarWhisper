package com.diparoy.lunarwhisper.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.http.SslCertificate.saveState
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.adapter.MessageAdapter
import com.diparoy.lunarwhisper.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.*

class ChatActivity : AppCompatActivity() {

    private var isStarFilled = true
    private var isSpamFilled = true
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var scrollView: ScrollView
    private lateinit var sharedPreferences: SharedPreferences

    //private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String

    //var receiverRoom: String? = null
    //var senderRoom: String? = null
    val senderUid = FirebaseAuth.getInstance().currentUser?.uid

    private fun updateStarInDatabase(isStarFilled: Boolean) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val starredMessagesRef = FirebaseDatabase.getInstance().getReference("/starred-messages/$currentUserUid/$receiverUid/$senderRoom")

            if (isStarFilled) {
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$senderUid/$receiverUid")
                latestMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val latestMessage = dataSnapshot.getValue(Message::class.java)

                        if (latestMessage != null) {
                            val chatroomData = mapOf(
                                "starred" to true,
                                "message" to latestMessage.message,
                                "timestamp" to latestMessage.timestamp
                            )
                            starredMessagesRef.setValue(chatroomData)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            } else {
                starredMessagesRef.removeValue()
            }
            saveState("starFilled_$senderRoom", isStarFilled)
        }
    }

    private fun updateStarredMessages(senderId: String, receiverId: String, message: Message) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val starredMessagesRef = FirebaseDatabase.getInstance().getReference("/starred-messages/$currentUserUid/$receiverUid/$senderRoom")

            if (isStarFilled) {
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$senderUid/$receiverUid")
                latestMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val latestMessage = dataSnapshot.getValue(Message::class.java)
                        starredMessagesRef.setValue(latestMessage)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        }
    }
    private fun fetchStarredStatus() {

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val starredMessagesRef = FirebaseDatabase.getInstance().getReference("/starred-messages/$currentUserUid/$receiverUid/$senderRoom")
            starredMessagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    isStarFilled = dataSnapshot.exists()
                    updateStarImage()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun updateSpamInDatabase(isSpamFilled: Boolean) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val spamMessagesRef = FirebaseDatabase.getInstance().getReference("/spam-messages/$currentUserUid/$receiverUid/$senderRoom")

            if (isSpamFilled) {
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$senderUid/$receiverUid")
                latestMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val latestMessage = dataSnapshot.getValue(Message::class.java)

                        if (latestMessage != null) {
                            val chatroomData = mapOf(
                                "starred" to true,
                                "message" to latestMessage.message,
                                "timestamp" to latestMessage.timestamp
                            )
                            spamMessagesRef.setValue(chatroomData)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            } else {
                spamMessagesRef.removeValue()
            }
            saveState("spamFilled_$senderRoom", isSpamFilled)
        }
    }

    private fun updateSpamMessages(senderId: String, receiverId: String, message: Message) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val spamMessagesRef = FirebaseDatabase.getInstance().getReference("/spam-messages/$currentUserUid/$receiverUid/$senderRoom")

            if (isSpamFilled) {
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$senderUid/$receiverUid")
                latestMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val latestMessage = dataSnapshot.getValue(Message::class.java)
                        spamMessagesRef.setValue(latestMessage)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        }
    }
    private fun fetchSpamStatus() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val spamMessagesRef = FirebaseDatabase.getInstance().getReference("/spam-messages/$currentUserUid/$receiverUid/$senderRoom")
            spamMessagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    isSpamFilled = dataSnapshot.exists()
                    updateSpamImage()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val recipientUsername = intent.getStringExtra("recipientUsername")
        val userImageUri = intent.getStringExtra("userImageUri")
        val recipientUid = intent.getStringExtra("recipientUid")

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val receiverUid = intent.getStringExtra("recipientUid")
        mDbRef = FirebaseDatabase.getInstance().reference

        val chatPartnerUid = intent.getStringExtra("recipientUid")

        if (senderUid != null) {
            if (senderUid < chatPartnerUid.toString()) {

                if (chatPartnerUid != null) {
                    this.receiverUid = (senderUid + chatPartnerUid).substring(28)
                }
            } else {
                if (chatPartnerUid != null) {
                    this.receiverUid = (senderUid + chatPartnerUid).substring(28)
                }
            }
        } else {

        }

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        scrollView = findViewById(R.id.scrollView)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        scrollView.postDelayed({
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }, 1)

        chatRecyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
        }

        messageBox.isFocusableInTouchMode = true

        mDbRef.child("chats").child(senderRoom!!).child("messages")
            //.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }

                    //messageList.reverse()

                    messageAdapter.notifyDataSetChanged()

                    chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        sendButton.setOnClickListener {
            val message = messageBox.text.toString().trim()
            if (message.isNotEmpty() && message.isNotBlank()) {
                val timestamp = getCurrentTimestamp()

                val senderId = senderUid!!
                val receiverId = receiverUid!!
                getUserType(senderId) { userType ->

                    val messageObject = Message(message, senderUid!!, receiverUid!!, recipientUsername!!, userImageUri!!, timestamp, userType)

                    updateMessageInChats(senderId, receiverId, messageObject)

                    val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$senderUid/$receiverUid")
                    latestMessagesRef.setValue(messageObject)

                    val latestMessagesToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$receiverUid/$senderUid")
                    latestMessagesToRef.setValue(messageObject)

                    mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                        .setValue(messageObject).addOnSuccessListener {
                            mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                                .setValue(messageObject)
                        }

                    messageBox.setText("")
                    messageBox.isFocusableInTouchMode = true

                    updateStarredMessages(senderId, receiverId, messageObject)

                    updateSpamMessages(senderId, receiverId, messageObject)

                    chatRecyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                        chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            }
        }

        sharedPreferences = getSharedPreferences("LunarWhisperPreferences", Context.MODE_PRIVATE)

        val spamImageView = findViewById<ImageView>(R.id.spam)
        val starImageView = findViewById<ImageView>(R.id.star)
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()


        // When the user opens the chat, retrieve the initial state from SharedPreferences
        /*val initialSpamState = getState("spamFilled_$senderRoom", false)
        isSpamFilled = initialSpamState
        updateSpamImage()

        val initialStarState = getState("starFilled_$senderRoom", false)
        isStarFilled = initialStarState
        updateStarImage()*/

        spamImageView.setOnClickListener {
            isSpamFilled = !isSpamFilled
            updateSpamImage()

            updateSpamInDatabase(isSpamFilled)
        }

        fetchSpamStatus()

        starImageView.setOnClickListener {
            isStarFilled = !isStarFilled
            updateStarImage()

            updateStarInDatabase(isStarFilled)
        }

        fetchStarredStatus()

        val userImage = findViewById<ImageView>(R.id.user_image)
        val userName = findViewById<TextView>(R.id.toolbarTitle)

        userName.text = recipientUsername
        if (userImageUri != null) {
            if (userImageUri.isNotEmpty()) {
                Glide.with(this)
                    .load(userImageUri)
                    .placeholder(R.drawable.img_4)
                    .error(R.drawable.img_4)
                    .into(userImage)
            } else {
                userImage.setImageResource(R.drawable.img_4)
            }
        }

        userName.setOnClickListener {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra("userName", recipientUsername)
            startActivity(profileIntent)
        }

        userImage.setOnClickListener {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra("userName", recipientUsername)
            startActivity(profileIntent)
        }
    }

    private fun getUserType(senderId: String, callback: (String) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$senderId/userType")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userType = snapshot.getValue(String::class.java) ?: "Primary"
                callback(userType)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun updateMessageInChats(senderId: String, receiverId: String, message: Message) {
        val senderRoom = "$senderId-$receiverId"
        val receiverRoom = "$receiverId-$senderId"
        //val senderMessagesRef = FirebaseDatabase.getInstance().getReference("/chats/$senderRoom/messages").push()
        //val receiverMessagesRef = FirebaseDatabase.getInstance().getReference("/chats/$receiverRoom/messages").push()

        val senderMessagesRef = FirebaseDatabase.getInstance().getReference("/chats/$senderRoom/messages").push()
        senderMessagesRef.setValue(message)

        if (senderRoom != receiverRoom) {
            val receiverMessagesRef = FirebaseDatabase.getInstance().getReference("/chats/$receiverRoom/messages").push()
            receiverMessagesRef.setValue(message)
        }

        //senderMessagesRef.setValue(message)
        //receiverMessagesRef.setValue(message)
    }

    private fun saveState(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun getState(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun formatTimestamp(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())

        return if (DateUtils.isToday(timestamp)) {
            sdf.format(calendar.time)
        } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
            "Yesterday"
        } else if (isWithinCurrentWeek(currentTime, timestamp)) {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        }
    }

    fun isWithinCurrentWeek(currentTime: Long, timestamp: Long): Boolean {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = currentTime

        val targetCalendar = Calendar.getInstance()
        targetCalendar.timeInMillis = timestamp

        return currentCalendar[Calendar.WEEK_OF_YEAR] == targetCalendar[Calendar.WEEK_OF_YEAR]
    }

    private fun updateStarImage() {
        val starImageView = findViewById<ImageView>(R.id.star)

        if (isStarFilled) {
            starImageView.setImageResource(R.drawable.star)
        } else {
            starImageView.setImageResource(R.drawable.star_outline)
        }
    }

    private fun updateSpamImage() {
        val spamImageView = findViewById<ImageView>(R.id.spam)

        if (isSpamFilled) {
            spamImageView.setImageResource(R.drawable.spam_filled)
        } else {
            spamImageView.setImageResource(R.drawable.spam)
        }
    }

    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy' 'HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
