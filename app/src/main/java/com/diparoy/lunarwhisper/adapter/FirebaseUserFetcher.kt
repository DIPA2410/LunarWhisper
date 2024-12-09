package com.diparoy.lunarwhisper.adapter

import com.diparoy.lunarwhisper.data.ClickedUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseUserFetcher(private val onDataFetched: (ClickedUser?) -> Unit) {

    fun fetchUserByUsername(username: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        val query = databaseReference.orderByChild("name").equalTo(username)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(ClickedUser::class.java)
                        onDataFetched(user)
                    }
                } else {
                    onDataFetched(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onDataFetched(null)
            }
        })
    }
}
