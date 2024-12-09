package com.diparoy.lunarwhisper.mainNavElements

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.diparoy.lunarwhisper.R

class SentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sent)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val title = findViewById<TextView>(R.id.toolbarTitle)
        title.text = "Sent"
    }
}