package com.diparoy.lunarwhisper.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.diparoy.lunarwhisper.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
        private const val USER_IMAGE_REQUEST_CODE = 3
        private const val PROFILE_BG_REQUEST_CODE = 4
    }

    private lateinit var saveProfileButton: Button
    private lateinit var user_name: EditText
    private lateinit var user_email: EditText
    private lateinit var user_bio: EditText
    private lateinit var user_details: EditText
    private lateinit var profile_background_image: ImageView
    private lateinit var user_image: CircleImageView
    private lateinit var fb_link: EditText
    private lateinit var insta_link: EditText
    private lateinit var edit_username_button: ImageButton
    private lateinit var edit_email_button: ImageButton
    private lateinit var edit_bio_button: ImageButton
    private lateinit var edit_user_details_button: ImageButton
    private lateinit var edit_fb_link_button: ImageButton
    private lateinit var edit_insta_link_button: ImageButton
    private lateinit var edit_profile_bg_button: ImageButton
    private lateinit var edit_user_image_button: ImageButton

    private var isEditing = false

    private val auth = Firebase.auth
    private val currentUser = auth.currentUser
    private val currentUserId = currentUser?.uid
    private val database = Firebase.database
    private val usersReference: DatabaseReference = database.reference.child("users")
    private val storage = Firebase.storage
    private val storageReference = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        saveProfileButton = findViewById(R.id.save_profile_button)
        user_name = findViewById(R.id.user_name)
        user_email = findViewById(R.id.user_email)
        user_bio = findViewById(R.id.user_bio)
        user_details = findViewById(R.id.user_details)
        fb_link = findViewById(R.id.fb_link)
        insta_link = findViewById(R.id.insta_link)
        profile_background_image = findViewById(R.id.profile_background_image)
        user_image = findViewById(R.id.user_image)
        edit_username_button = findViewById(R.id.edit_username_button)
        edit_email_button = findViewById(R.id.edit_email_button)
        edit_bio_button = findViewById(R.id.edit_bio_button)
        edit_user_details_button = findViewById(R.id.edit_user_details_button)
        edit_fb_link_button = findViewById(R.id.edit_fb_link_button)
        edit_insta_link_button = findViewById(R.id.edit_insta_link_button)
        edit_profile_bg_button = findViewById(R.id.edit_profile_bg_button)
        edit_user_image_button = findViewById(R.id.edit_user_image_button)

        fetchAndDisplayUserData()

        edit_user_image_button.setOnClickListener {
            openImageChooser(USER_IMAGE_REQUEST_CODE)
        }

        edit_profile_bg_button.setOnClickListener {
            openImageChooser(PROFILE_BG_REQUEST_CODE)
        }

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Profile"

        user_name.isEnabled = false
        user_email.isEnabled = false
        user_bio.isEnabled = false
        user_details.isEnabled = false
        fb_link.isEnabled = false
        insta_link.isEnabled = false

        edit_username_button.visibility = View.GONE
        edit_email_button.visibility = View.GONE
        edit_bio_button.visibility = View.GONE
        edit_user_details_button.visibility = View.GONE
        edit_fb_link_button.visibility = View.GONE
        edit_insta_link_button.visibility = View.GONE
        edit_profile_bg_button.visibility = View.GONE
        edit_user_image_button.visibility = View.GONE

        saveProfileButton.text = "EDIT PROFILE"

        saveProfileButton.setOnClickListener {
            if (isEditing) {

                val fbLinkInput = fb_link.text.toString().trim()
                if (fbLinkInput.isNotEmpty() && !isValidFacebookProfileLink(fbLinkInput)) {
                    fb_link.error = "Invalid Facebook profile link"
                    return@setOnClickListener
                }

                val instaLinkInput = insta_link.text.toString().trim()
                if (instaLinkInput.isNotEmpty() && !isValidInstagramProfileLink(instaLinkInput)) {
                    insta_link.error = "Invalid Instagram profile link"
                    return@setOnClickListener
                }

                saveUserData()

                fetchAndDisplayUserData()

                setEditingEnabled(false)

                saveProfileButton.text = "EDIT PROFILE"
            } else {
                setEditingEnabled(true)

                saveProfileButton.text = "SAVE PROFILE"
            }

            isEditing = !isEditing
        }
    }

    private fun openImageChooser(requestCode: Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, requestCode)
    }

    private fun saveUserData() {
        currentUserId?.let {
            val updatedName = user_name.text.toString()
            val updatedEmail = user_email.text.toString()
            val updatedBio = user_bio.text.toString()
            val updatedDetails = user_details.text.toString()
            val updatedFbLink = fb_link.text.toString()
            val updatedInstaLink = insta_link.text.toString()

            usersReference.child(it).apply {
                child("name").setValue(updatedName)
                child("email").setValue(updatedEmail)
                child("bio").setValue(updatedBio)
                child("details").setValue(updatedDetails)
                child("fbLink").setValue(updatedFbLink)
                child("instaLink").setValue(updatedInstaLink)
            }
        }
    }

    private fun fetchAndDisplayUserData() {
        currentUserId?.let { userId ->
            val userRef = usersReference.child(userId)
            userRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val userName = dataSnapshot.child("name").value.toString()
                    val userEmail = dataSnapshot.child("email").value.toString()
                    val userImageUri = dataSnapshot.child("imageUri").value.toString()

                    val profileBackgroundImage = dataSnapshot.child("profileBackgroundImage").value.toString()
                    val userBio = dataSnapshot.child("bio").value.toString()
                    val userDetails = dataSnapshot.child("details").value.toString()
                    val userFbLink = dataSnapshot.child("fbLink").value.toString()
                    val userInstaLink = dataSnapshot.child("instaLink").value.toString()

                    user_name.setText(userName)
                    user_email.setText(userEmail)
                    user_bio.setText(userBio)
                    user_details.setText(userDetails)
                    fb_link.setText(userFbLink)
                    insta_link.setText(userInstaLink)

                    if (userBio == "null") {
                        user_bio.hint = "Bio: Occupation | Hobby"
                        user_bio.text = null
                    } else {
                        user_bio.setText(userBio)
                    }

                    if (userDetails == "null") {
                        user_details.hint = "Details: Age | Location"
                        user_details.text = null
                    } else {
                        user_details.setText(userDetails)
                    }

                    if (userFbLink == "null") {
                        fb_link.hint = "Facebook Profile Link"
                        fb_link.text = null
                    } else {
                        fb_link.setText(userFbLink)
                    }

                    if (userInstaLink == "null") {
                        insta_link.hint = "Instagram Profile Link"
                        insta_link.text = null
                    } else {
                        insta_link.setText(userInstaLink)
                    }

                    Glide.with(this)
                        .load(userImageUri)
                        .placeholder(R.drawable.img_4)
                        .error(R.drawable.img_4)
                        .into(user_image)

                    Glide.with(this)
                        .load(profileBackgroundImage)
                        .placeholder(R.drawable.profile_card_bg)
                        .error(R.drawable.profile_card_bg)
                        .into(profile_background_image)
                }
            }
        }
    }

    private fun setEditingEnabled(enabled: Boolean) {
        user_name.isEnabled = enabled
        user_email.isEnabled = enabled
        user_bio.isEnabled = enabled
        user_details.isEnabled = enabled
        fb_link.isEnabled = enabled
        insta_link.isEnabled = enabled

        val visibility = if (enabled) View.VISIBLE else View.GONE
        edit_username_button.visibility = visibility
        edit_email_button.visibility = visibility
        edit_bio_button.visibility = visibility
        edit_user_details_button.visibility = visibility
        edit_fb_link_button.visibility = visibility
        edit_insta_link_button.visibility = visibility
        edit_profile_bg_button.visibility = visibility
        edit_user_image_button.visibility = visibility
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {

                }

                USER_IMAGE_REQUEST_CODE -> {

                    val imageUri = data.data
                    user_image.setImageURI(imageUri)
                }

                PROFILE_BG_REQUEST_CODE -> {

                    val imageUri = data.data
                    profile_background_image.setImageURI(imageUri)
                }
            }
        }

        if (requestCode == USER_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val imageUri = data.data
                if (imageUri != null) {
                    val imageRef = storageReference.child("profile_images/$currentUserId")
                    imageRef.putFile(imageUri).addOnCompleteListener { uploadTask ->
                        if (uploadTask.isSuccessful) {
                            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                val imageUriString = downloadUri.toString()
                                if (currentUserId != null) {
                                    usersReference.child(currentUserId).child("imageUri")
                                        .setValue(imageUriString)
                                }

                                Glide.with(this)
                                    .load(imageUriString)
                                    .into(user_image)
                            }
                        } else {

                        }
                    }
                }
            }
        }


        if (requestCode == PROFILE_BG_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val imageUri = data.data
                if (imageUri != null) {
                    val imageRef = storageReference.child("profile_background_images/$currentUserId")
                    imageRef.putFile(imageUri).addOnCompleteListener { uploadTask ->
                        if (uploadTask.isSuccessful) {
                            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                val bgImageUriString = downloadUri.toString()
                                if (currentUserId != null) {
                                    usersReference.child(currentUserId)
                                        .child("profileBackgroundImage").setValue(bgImageUriString)
                                }

                                Glide.with(this)
                                    .load(bgImageUriString)
                                    .into(profile_background_image)
                            }
                        } else {

                        }
                    }
                }
            }
        }
    }

    private fun isValidFacebookProfileLink(input: String): Boolean {
        val facebookProfilePattern = "https?://(www\\.)?facebook\\.com/profile\\.php\\?id=\\d+".toRegex()
        return facebookProfilePattern.matches(input)
    }

    private fun isValidInstagramProfileLink(input: String): Boolean {
        val instagramProfilePattern = "^https://www\\.instagram\\.com/[a-zA-Z0-9_.]+/?\$".toRegex()
        return instagramProfilePattern.matches(input)
    }

    private fun requestCameraPermission() {
        val permission = android.Manifest.permission.CAMERA
        val granted = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(this, permission) != granted) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), CAMERA_REQUEST_CODE)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            } else {

            }
        }
    }
}
