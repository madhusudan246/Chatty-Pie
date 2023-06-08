package com.example.chatty.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chatty.adapters.ConversationAdapter
import com.example.chatty.databinding.FragmentChatsBinding
import com.example.chatty.modals.ConversationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore


class Chats : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var conversationArrayList: ArrayList<ConversationData>
    private lateinit var fStore: FirebaseFirestore
    private val uniqueUserIds: HashSet<String> = HashSet()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding?.root

        conversationArrayList = arrayListOf()
        fStore = FirebaseFirestore.getInstance()

        conversationAdapter = ConversationAdapter(context as? Activity, conversationArrayList)

        binding?.recyclerViewChats?.adapter = conversationAdapter

        binding?.recyclerViewChats?.setHasFixedSize(true)

        val documentRef = fStore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .collection("Chats")

        documentRef.addSnapshotListener { value, error ->
            if(error!=null){
                Log.d("Error", "Unable to fetch data")
            }
            else {
                value?.documentChanges?.forEach { documentChange ->
                    val userId = documentChange.document.id

                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            if (!uniqueUserIds.contains(userId)) {
                                uniqueUserIds.add(userId)

                                fStore.collection("users")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val name = documentSnapshot.getString("Name").toString()
                                        val userProfilePhoto =
                                            documentSnapshot.getString("userProfilePhoto")
                                                .toString()

                                        val user = ConversationData(name, userProfilePhoto, userId)
                                        conversationArrayList.add(user)

                                        conversationAdapter.notifyDataSetChanged()
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d("Error", "Unable to fetch data: $exception")
                                    }
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            uniqueUserIds.remove(userId)

                            val removedUserIndex =
                                conversationArrayList.indexOfFirst { it.uid == userId }
                            if (removedUserIndex != -1) {
                                conversationArrayList.removeAt(removedUserIndex)
                                conversationAdapter.notifyItemRemoved(removedUserIndex)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            if (uniqueUserIds.contains(userId)) {
                                val modifiedUserIndex =
                                    conversationArrayList.indexOfFirst { it.uid == userId }
                                if (modifiedUserIndex != -1) {
                                    fStore.collection("users")
                                        .document(userId)
                                        .get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            val name = documentSnapshot.getString("Name").toString()
                                            val userProfilePhoto =
                                                documentSnapshot.getString("userProfilePhoto")
                                                    .toString()

                                            val modifiedUser =
                                                ConversationData(name, userProfilePhoto, userId)
                                            conversationArrayList[modifiedUserIndex] = modifiedUser

                                            conversationAdapter.notifyItemChanged(modifiedUserIndex)
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.d("Error", "Unable to fetch data: $exception")
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }

        return view
    }

}