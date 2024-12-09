package com.diparoy.lunarwhisper.ui


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.data.User
import com.diparoy.lunarwhisper.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: Dialog
    private var isPasswordVisible = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()

        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        progressDialog = Dialog(this@LoginActivity)
        progressDialog.setContentView(R.layout.custom_progress_dialog)
        progressDialog.setCancelable(false)

        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        sharedPreferences = getSharedPreferences("loginState", Context.MODE_PRIVATE)

        binding.gotoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.forgotPass.setOnClickListener {
            val email = binding.emailEdt.text.toString()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this@LoginActivity, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEdt.text.toString()
            val password = binding.passEdt.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                logIn(email, password)
            } else{
                Toast.makeText(this@LoginActivity, "Please Enter Email and Password", Toast.LENGTH_SHORT).show()
            }
        }

        setupPasswordToggle()
    }

    private fun sendPasswordResetEmail(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun logIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Please enter both Email and Password", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null) {
                        getUserData(user.uid)
                    }
                } else {
                    progressDialog.dismiss()
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this@LoginActivity, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@LoginActivity, "Incorrect Email or Password", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Sign-in Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun getUserData(uid: String) {
        databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        progressDialog.dismiss()
                        saveLoginState(true)
                        navigateToMainActivity()
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    private fun checkLoginState(): Boolean {
        val sharedPreferences = getSharedPreferences("loginState", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun navigateToMainActivity() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {

        }
    }

    private fun setupPasswordToggle() {
        binding.passToggle.setOnCheckedChangeListener { _, isChecked ->
            val inputType = if (isChecked) {
                isPasswordVisible = true
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                isPasswordVisible = false
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.passEdt.inputType = inputType
            binding.passEdt.setSelection(binding.passEdt.text.length)
            binding.passEdt.typeface = Typeface.MONOSPACE
        }
        binding.passEdt.typeface = Typeface.MONOSPACE
    }
}
