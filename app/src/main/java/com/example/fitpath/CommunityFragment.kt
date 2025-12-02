package com.example.fitpath

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommunityFragment : Fragment(R.layout.fragment_community) {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val users = mutableListOf<User>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.rvUsers)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserAdapter(users) { user ->
            // Handle user click (e.g., add friend)
        }
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchUsers(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchUsers(it) }
                return true
            }
        })

        loadFriends()
    }

    private fun searchUsers(query: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                users.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    if (user.id != auth.currentUser?.uid) { // Exclude current user
                        users.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    // Load friends from Firestore
    private fun loadFriends() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    users.clear()
                    for (document in documents) {
                        val friendId = document.id
                        db.collection("users").document(friendId).get()
                            .addOnSuccessListener { friendDoc ->
                                if (friendDoc.exists()) {
                                    val friend = friendDoc.toObject(User::class.java)
                                    users.add(friend!!)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
        }
    }
}
