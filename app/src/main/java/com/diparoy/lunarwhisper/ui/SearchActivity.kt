package com.diparoy.lunarwhisper.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.adapter.SearchAdapter
import com.diparoy.lunarwhisper.data.ClickedUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchActivity : AppCompatActivity(), SearchAdapter.OnItemClickListener {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: SearchAdapter
    private lateinit var searchEditText: EditText

    private var allUsers: List<ClickedUser> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val customToolbar = findViewById<Toolbar>(R.id.customToolbar)
        setSupportActionBar(customToolbar)
        actionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = null

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Search"

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        window.statusBarColor = ContextCompat.getColor(this, R.color.skin)


        searchEditText = findViewById(R.id.searchEditText)

        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        adapter = SearchAdapter(this, ArrayList(), this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = ArrayList<ClickedUser>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(ClickedUser::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }

                allUsers = users
                adapter.updateData(allUsers)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adapter.updateData(allUsers)
                }
            }
        })
    }

    override fun onItemClick(clickedUser: ClickedUser) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userName", clickedUser.name)
        startActivity(intent)
    }
}
