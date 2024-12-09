package com.diparoy.lunarwhisper.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.data.User
import com.diparoy.lunarwhisper.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val storageReference = FirebaseStorage.getInstance().reference
    private lateinit var progressDialog: Dialog
    private var selectedImageUri: Uri? = null
    private lateinit var defaultImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()

        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        progressDialog = Dialog(this@RegisterActivity)
        progressDialog.setContentView(R.layout.custom_progress_dialog)
        progressDialog.setCancelable(false)

        defaultImageUri = Uri.parse("android.resource://$packageName/${R.drawable.img_4}")

        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        binding.profileImage.setOnClickListener {
            openImageChooser()
        }

        val profileBackgroundImage = findViewById<ImageView>(R.id.profile_background_image)

        binding.signupBtn.setOnClickListener {
            val name = binding.regName.text.toString()
            val email = binding.regEmail.text.toString()
            val password = binding.regPassEdt.text.toString()

            if (name.length <= 20 && email.isNotEmpty() && password.isNotEmpty()) {
                signUp(name, email, password)
            } else if (name.length > 20) {
                Toast.makeText(this@RegisterActivity, "Name should not exceed 20 characters", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@RegisterActivity, "Please Enter Name, Email, and Password", Toast.LENGTH_SHORT).show()
            }
        }

        setupPasswordToggle()

        selectedImageUri = defaultImageUri
        binding.profileImage.setImageURI(defaultImageUri)
    }


    private fun signUp(name: String, email: String, password: String) {
        databaseReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressDialog.show()
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this@RegisterActivity) { task ->
                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            user?.uid?.let { uid ->
                                selectedImageUri?.let { uri ->
                                    val imageRef = storageReference.child("profile_images/$uid")

                                    imageRef.putFile(uri).addOnCompleteListener { uploadTask ->
                                        if (uploadTask.isSuccessful) {
                                            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                                val imageUri = downloadUri.toString()
                                                val status = "Hey There! I'm using LunarWhisper!"
                                                val profileBackgroundImage = "https://firebasestorage.googleapis.com/v0/b/lunarwhisper-c166f.appspot.com/o/profile_card_bg.png?alt=media&token=c1c692a8-2429-4633-bbbc-4e1aa490d55b"

                                                val userObj = User(uid, name, email, imageUri, status, fbLink = null, instaLink = null, profileBackgroundImage, userType = null)
                                                addUserToDatabase(userObj)
                                                progressDialog.dismiss()
                                                Toast.makeText(this@RegisterActivity, "Signup Successful", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                        } else {
                                            progressDialog.dismiss()
                                            Toast.makeText(this@RegisterActivity, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else {
                    progressDialog.dismiss()
                    Toast.makeText(this@RegisterActivity, "Username is taken, please try another.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@RegisterActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addUserToDatabase(user: User) {
        val userReference = databaseReference.child(user.uid ?: "")

        userReference.setValue(user).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                progressDialog.dismiss()
                Toast.makeText(this@RegisterActivity, "Error adding user data", Toast.LENGTH_SHORT).show()
            } else {
                progressDialog.dismiss()
            }
        }
    }

    private fun setupPasswordToggle() {
        binding.regPassToggle.setOnCheckedChangeListener { _, isChecked ->
            val inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.regPassEdt.inputType = inputType
            binding.regPassEdt.setSelection(binding.regPassEdt.text.length)
            binding.regPassEdt.typeface = Typeface.MONOSPACE
        }
        binding.regPassEdt.typeface = Typeface.MONOSPACE
    }

    private val imageChooserLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.profileImage.setImageURI(uri)
        }
    }

    private fun openImageChooser() {
        imageChooserLauncher.launch("image/*")
    }
}
