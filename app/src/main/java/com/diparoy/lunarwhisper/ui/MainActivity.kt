package com.diparoy.lunarwhisper.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.adapter.MainPagerAdapter
import com.diparoy.lunarwhisper.data.User
import com.diparoy.lunarwhisper.databinding.ActivityMainBinding
import com.diparoy.lunarwhisper.fragment.ChatFragment
import com.diparoy.lunarwhisper.fragment.DrawerFragment
import com.diparoy.lunarwhisper.fragment.XOXOFragment
import com.diparoy.lunarwhisper.mainNavElements.PromotionalActivity
import com.diparoy.lunarwhisper.mainNavElements.SpamActivity
import com.diparoy.lunarwhisper.mainNavElements.StarActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var currentUser: FirebaseUser
    private lateinit var userReference: DatabaseReference
    private lateinit var mainUserImage: CircleImageView
    private lateinit var searchIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navUserName: TextView
    private lateinit var navUserEmail: TextView
    private lateinit var navUserImage: CircleImageView
    private lateinit var radioPrimary: RadioButton
    private lateinit var radioPromotional: RadioButton
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        tabLayout = binding.tabLayout
        mainUserImage = binding.mainUserImage
        searchIcon = binding.searchIcon

        val sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("nightMode", false)

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        mainUserImage.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = binding.navigationView

        val navHeader = navigationView.getHeaderView(0)
        val navUserName = navHeader?.findViewById<TextView>(R.id.navUserName)
        val navUserEmail = navHeader?.findViewById<TextView>(R.id.navUserEmail)
        val navUserImage = navHeader?.findViewById<CircleImageView>(R.id.navUserImage)

        navUserName?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        navUserEmail?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        navUserImage?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()

        currentUser = auth.currentUser!!
        userReference = database.reference.child("users").child(currentUser.uid)

        setupViewPager()
        loadUserImage()
        if (navUserName != null) {
            if (navUserEmail != null) {
                if (navUserImage != null) {
                    loadUserData(navUserName, navUserEmail, navUserImage)
                }
            }
        }
        setupNavigationDrawer()
        setListeners()

        showPopupMessage()
    }

    private fun setupViewPager() {
        val viewPager = binding.viewPager
        val adapter = MainPagerAdapter(this)
        adapter.addFragment(ChatFragment(), "Chat")
        adapter.addFragment(XOXOFragment(), "XOXO")

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }

    private fun loadUserData(
        navUserName: TextView,
        navUserEmail: TextView,
        navUserImage: CircleImageView
    ) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid)
            userReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        val imageUri = user.imageUri
                        if (!imageUri.isNullOrEmpty()) {
                            Picasso.get()
                                .load(user.imageUri)
                                .placeholder(R.drawable.img_4)
                                .error(R.drawable.img_4)
                                .into(navUserImage)
                        } else {
                            navUserImage.setImageResource(R.drawable.img_4)
                        }

                        navUserName.text = user.name
                        navUserEmail.text = user.email
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
    }

    private fun loadUserImage() {
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                user?.let {
                    val imageUri = user.imageUri
                    if (!imageUri.isNullOrEmpty()) {
                        Picasso.get()
                            .load(user.imageUri)
                            .placeholder(R.drawable.img_4)
                            .error(R.drawable.img_4)
                            .into(mainUserImage)
                    } else {
                        mainUserImage.setImageResource(R.drawable.img_4)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val drawerFragment = DrawerFragment()
        fragmentTransaction.replace(R.id.navigationView, drawerFragment)
        fragmentTransaction.commit()
    }

    private fun setListeners() {
        mainUserImage.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    val intent = Intent(this, EditProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.starred -> {
                    val intent = Intent(this, StarActivity::class.java)
                    startActivity(intent)
                }
                R.id.spam -> {
                    val intent = Intent(this, SpamActivity::class.java)
                    startActivity(intent)
                }
                /*R.id.inbox->{
                    val intent = Intent(this, InboxActivity::class.java)
                    startActivity(intent)
                }*/
                R.id.promotion->{
                    val intent = Intent(this, PromotionalActivity::class.java)
                    startActivity(intent)
                }
                /*R.id.sent->{
                    val intent = Intent(this, SentActivity::class.java)
                    startActivity(intent)
                }*/
                R.id.action_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
            true
        }

        searchIcon.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPopupMessage() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val hasShownDialog = sharedPreferences.getBoolean("hasShownDialog", false)

        if (!hasShownDialog) {
            val customView = layoutInflater.inflate(R.layout.radio_dialog, null)
            val builder = AlertDialog.Builder(this)
            builder.setView(customView)

            val dialog = builder.create()
            dialog.show()

            val message = customView.findViewById<TextView>(R.id.dialog_message)
            val radioGroup = customView.findViewById<RadioGroup>(R.id.options_radio_group)
            radioPrimary = customView.findViewById(R.id.radio_primary)
            radioPromotional = customView.findViewById(R.id.radio_promotional)

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_primary -> {
                        updateAndSaveUserType("Primary")
                    }
                    R.id.radio_promotional -> {
                        updateAndSaveUserType("Promotional")
                    }
                }
                Handler().postDelayed({
                    dialog.dismiss()

                    val editor = sharedPreferences.edit()
                    editor.putBoolean("hasShownDialog", true)
                    editor.apply()
                }, 1000)
            }

            dialog.setOnDismissListener {
                if (radioGroup.checkedRadioButtonId == -1) {
                    updateAndSaveUserType("Primary")
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("hasShownDialog", true)
                    editor.apply()
                }
            }
        }
    }

    private fun updateAndSaveUserType(userType: String) {
        userReference.child("userType").setValue(userType)
        when (userType) {
            "Primary" -> {
                radioPrimary.isChecked = true
            }
            "Promotional" -> {
                radioPromotional.isChecked = true
            }
        }
    }

    private fun logoutUser() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to Logout?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            dialog.dismiss()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.dark_brown))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.dark_brown))
        }
        dialog.show()
    }
}
