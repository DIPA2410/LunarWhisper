package com.diparoy.lunarwhisper.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.diparoy.lunarwhisper.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : AppCompatActivity() {
    private lateinit var userTypeOptions: RadioGroup
    private lateinit var currentUser: FirebaseAuth
    private lateinit var userReference: DatabaseReference

    private lateinit var switchMode: SwitchCompat
    private var nightMode: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var deleteAccountTextView: TextView
    private lateinit var changeAccountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Settings"

        deleteAccountTextView = findViewById(R.id.deleteAccount)

        deleteAccountTextView.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        changeAccountTextView = findViewById(R.id.changeAccount)

        changeAccountTextView.setOnClickListener {
            showChangeAccountEmailDialog()
        }

        switchMode = findViewById(R.id.switchMode)
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        nightMode = sharedPreferences.getBoolean("nightMode", false)

        if (nightMode) {
            switchMode.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        switchMode.setOnClickListener {
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor = sharedPreferences.edit()
                editor.putBoolean("nightMode", false)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor = sharedPreferences.edit()
                editor.putBoolean("nightMode", true)
            }
            editor.apply()
        }

        currentUser = FirebaseAuth.getInstance()
        userReference =
            currentUser.uid?.let { FirebaseDatabase.getInstance().reference.child("users").child(it) }!!

        userTypeOptions = findViewById(R.id.userTypeOptions)
        val primaryUserRadioButton = findViewById<RadioButton>(R.id.primaryUser)
        val promotionalUserRadioButton = findViewById<RadioButton>(R.id.promotionalUser)

        userReference.child("userType").get().addOnSuccessListener { dataSnapshot ->
            when (dataSnapshot.getValue(String::class.java)) {
                "Primary User" -> primaryUserRadioButton.isChecked = true
                "Promotional User" -> promotionalUserRadioButton.isChecked = true
                else -> primaryUserRadioButton.isChecked = true
            }
        }

        userTypeOptions.setOnCheckedChangeListener { _, checkedId ->
            val selectedUserType = when (checkedId) {
                R.id.primaryUser -> "Primary User"
                R.id.promotionalUser -> "Promotional User"
                else -> "Primary User"
            }
            userReference.child("userType").setValue(selectedUserType)
        }
    }

    private fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    private fun showChangeAccountEmailDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
        builder.setTitle("Change Account")
        builder.setMessage("Are you sure you want to change your account? (You may need to re-login to Change your Account) If Yes, please enter your new email address:")

        val currentEmailInput = TextView(this)
        val currentEmailLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val marginDp = 10
        val marginPx = marginDp.dpToPx(this)
        currentEmailLayoutParams.setMargins(marginPx, 10, marginPx, 5)
        currentEmailLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
        currentEmailInput.layoutParams = currentEmailLayoutParams
        currentEmailInput.text = FirebaseAuth.getInstance().currentUser?.email
        currentEmailInput.textSize = 18f
        currentEmailInput.setTextColor(Color.BLACK)

        val newEmailInput = EditText(this)
        val newEmailLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val marginInDp = 5
        val marginInPx = marginInDp.dpToPx(this)
        newEmailLayoutParams.setMargins(marginInPx, 0, marginInPx, 0)
        newEmailLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
        newEmailInput.layoutParams = newEmailLayoutParams
        newEmailInput.setTextColor(Color.BLACK)
        newEmailInput.setHintTextColor(Color.DKGRAY)
        newEmailInput.hint = "New Email Address"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(currentEmailInput)
        layout.addView(newEmailInput)

        builder.setView(layout)

        builder.setPositiveButton("Submit") { _, _ ->
            val currentEmail = currentEmailInput.text.toString()
            val newEmail = newEmailInput.text.toString()

            if (currentEmail.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Please fill in both email fields", Toast.LENGTH_SHORT).show()
            } else {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                if (newEmail == currentUser?.email) {
                    Toast.makeText(this, "New email is the same as the current email", Toast.LENGTH_SHORT).show()
                } else {
                    auth.fetchSignInMethodsForEmail(newEmail)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result?.signInMethods
                                if (signInMethods.isNullOrEmpty()) {
                                    currentUser?.updateEmail(newEmail)
                                        ?.addOnCompleteListener { updateTask ->
                                            if (updateTask.isSuccessful) {
                                                userReference.child("email").setValue(newEmail)

                                                currentUser.sendEmailVerification()
                                                    .addOnCompleteListener { verificationTask ->
                                                        if (verificationTask.isSuccessful) {
                                                            val verificationMessage =
                                                                "Email updated successfully. A verification email has been sent to your new email address. Please verify your new email to complete the process.\n" +
                                                                        "Once verified, you can log in with your new email."
                                                            FirebaseAuth.getInstance().signOut()

                                                            val intent = Intent(this, LoginActivity::class.java)
                                                            startActivity(intent)
                                                            finish()

                                                            Toast.makeText(this, verificationMessage, Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(
                                                                this,
                                                                "Email updated, but failed to send the verification email: ${verificationTask.exception?.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                            } else {
                                                val exception = updateTask.exception
                                                Toast.makeText(this, "Email update failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(this, "That email address is already in use. Please choose a different email address.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.dark_brown))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.dark_brown))
        }
        dialog.show()
    }

    private fun showDeleteAccountConfirmationDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
        builder.setTitle("Delete Account")
        builder.setMessage("Do you really want to Delete your Account? Note that all the data of your account will be Deleted, which won't be able to get recovered. You may need to re-login to Delete you Account.")
        builder.setPositiveButton("Yes") { _, _ ->
            val user = FirebaseAuth.getInstance().currentUser
            user?.delete()
                ?.addOnSuccessListener {
                    val userId = user.uid
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    databaseReference.child("users").child(userId).removeValue()

                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to delete the account: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog?.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.dark_brown))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.dark_brown))
        }
        dialog.show()
    }
}
