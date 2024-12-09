package com.diparoy.lunarwhisper.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.adapter.FirebaseUserFetcher
import com.diparoy.lunarwhisper.data.ClickedUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var profileBackgroundImageView: ImageView
    private lateinit var userImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val username = intent.getStringExtra("userName")

        userNameTextView = findViewById(R.id.user_name)
        userEmailTextView = findViewById(R.id.user_email)
        profileBackgroundImageView = findViewById(R.id.profile_background_image)
        userImageView = findViewById(R.id.user_image)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val messageButton = findViewById<Button>(R.id.message_button)
        messageButton.setOnClickListener {
            val chatIntent = Intent(this, ChatActivity::class.java)

            val username = intent.getStringExtra("userName")

            if (username != null) {
                fetchUserDataByUsername(username) { user ->
                    if (user != null) {
                        chatIntent.putExtra("recipientUsername", user.name ?: "")
                        chatIntent.putExtra("userImageUri", user.imageUri ?: "")
                        chatIntent.putExtra("recipientUid", user.uid ?: "")
                        startActivity(chatIntent)
                    } else {

                    }
                }
            }
        }

        if (!username.isNullOrEmpty()) {
            val title = findViewById<TextView>(R.id.toolbarTitle)
            title.text = username
        }

        val firebaseUserFetcher = FirebaseUserFetcher { clickedUser ->
            if (clickedUser != null) {
                updateUIWithUserData(clickedUser)
            } else {
                handleUserNotFound()
            }
        }

        if (username != null) {
            firebaseUserFetcher.fetchUserByUsername(username)
        }
    }

    private fun updateUIWithUserData(user: ClickedUser) {
        userNameTextView.text = user.name
        userEmailTextView.text = user.email

        Glide.with(this)
            .load(user.profileBackgroundImage)
            .placeholder(R.drawable.profile_card_bg)
            .error(R.drawable.profile_card_bg)
            .into(profileBackgroundImageView)

        Glide.with(this)
            .load(user.imageUri)
            .placeholder(R.drawable.img_4)
            .error(R.drawable.img_4)
            .into(userImageView)

        val userBioTextView = findViewById<TextView>(R.id.user_bio)
        val userDetailsTextView = findViewById<TextView>(R.id.user_details)

        userBioTextView.text = user.bio?.takeIf { it.isNotEmpty() } ?: "Blank user Bio"
        userDetailsTextView.text = user.details?.takeIf { it.isNotEmpty() } ?: "Blank user Details"

        findViewById<ImageView>(R.id.email_image).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "text/plain"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(user.email))
            startActivity(Intent.createChooser(emailIntent, "Send email"))
        }

        user.fbLink?.let { fbLink ->
            findViewById<ImageView>(R.id.fb_image).setOnClickListener {
                if (fbLink.isNotEmpty()) {
                    val fbIntent = Intent(Intent.ACTION_VIEW)
                    fbIntent.data = fbLink.toUri()
                    startActivity(fbIntent)
                } else {
                    showToast("No Facebook Link Found")
                }
            }
        }

        user.instaLink?.let { instaLink ->
            findViewById<ImageView>(R.id.insta_image).setOnClickListener {
                if (instaLink.isNotEmpty()) {
                    val instaIntent = Intent(Intent.ACTION_VIEW)
                    instaIntent.data = instaLink.toUri()
                    startActivity(instaIntent)
                } else {
                    showToast("No Instagram Link Found")
                }
            }
        }
    }

    private fun fetchUserDataByUsername(username: String, callback: (ClickedUser?) -> Unit) {

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.orderByChild("name").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(ClickedUser::class.java)
                        if (user != null) {
                            callback(user)
                            return
                        }
                    }
                }
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    private fun handleUserNotFound() {

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
