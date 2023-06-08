package com.example.chatty.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.chatty.R
import com.example.chatty.adapters.MessagesAdapter
import com.example.chatty.databinding.FragmentMessageBinding
import com.example.chatty.modals.MessageData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.Date

class Message : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding
    private lateinit var toolbarMessage: androidx.appcompat.widget.Toolbar
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageArrayList: ArrayList<MessageData>
    private lateinit var fStore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        val view = binding?.root

        messageArrayList = arrayListOf()
        fStore = FirebaseFirestore.getInstance()

        val recieverUid = activity?.intent?.getStringExtra("Uid")
        val recieverName = activity?.intent?.getStringExtra("Name")
        val recieverImg = activity?.intent?.getStringExtra("RecieverProfilePic")

        val senderUid = FirebaseAuth.getInstance().uid

        binding?.userName?.text = recieverName
        Picasso.get().load(recieverImg).placeholder(R.drawable.no_profile).error(R.drawable.no_profile).into(binding?.userImg)

        val activity = this.activity
        if (activity != null) {
            toolbarMessage=activity.findViewById(R.id.toolbarMenu)
        }

        toolbarMessage.visibility = View.GONE

        binding?.backArrowMessage?.drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.grey))

        binding?.chatMessage?.hint = "Message..."

        binding?.backArrowMessage?.setOnClickListener {
            binding?.backArrowMessage?.drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.splash))
            activity?.finish()
        }

        val documentRef = fStore.collection("users")
            .document(senderUid.toString())
            .collection("Chats")
            .document(recieverUid.toString())


        documentReference = fStore.collection("users")
            .document(senderUid.toString())
            .collection("Chats")
            .document(recieverUid.toString())


        binding?.sendBtn?.setOnClickListener {
            if(binding?.chatMessage?.text.toString()!="") {
                val messageTxt = binding?.chatMessage?.text
                binding?.chatMessage?.setText("")


                val date = Date()
                val message = MessageData(messageTxt.toString(), senderUid.toString(), date.time)
                val msg = hashMapOf(
                    "message" to arrayListOf(message),
                )

                documentRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            if (document.exists()) {

                                documentRef.update("message", FieldValue.arrayUnion(message))
                                    .addOnSuccessListener {
                                        val documentRef2 = fStore.collection("users")
                                            .document(recieverUid.toString())
                                            .collection("Chats")
                                            .document(senderUid.toString())

                                        documentRef2.get().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val document2 = task.result
                                                if (document2 != null) {
                                                    if (document2.exists()) {

                                                        documentRef2.update(
                                                            "message",
                                                            FieldValue.arrayUnion(message)
                                                        )
                                                        Log.d("TAG", "Document already exists.")
                                                    } else {

                                                        documentRef2.set(msg)
                                                        Log.d("TAG", "Document doesn't exist.")
                                                    }
                                                }
                                            } else {
                                                Log.d("TAG", "Error: ", task.exception)
                                            }
                                        }
                                    }
                                Log.d("TAG", "Document already exists.")
                            } else {

                                documentRef.set(msg)
                                    .addOnSuccessListener {
                                        val documentRef2 = fStore.collection("users")
                                            .document(recieverUid.toString())
                                            .collection("Chats")
                                            .document(senderUid.toString())

                                        documentRef2.get().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val document2 = task.result
                                                if (document2 != null) {
                                                    if (document2.exists()) {
                                                        documentRef2.update(
                                                            "message",
                                                            FieldValue.arrayUnion(message)
                                                        )
                                                        Log.d("TAG", "Document already exists.")
                                                    } else {

                                                        documentRef2.set(msg)
                                                        Log.d("TAG", "Document doesn't exist.")
                                                    }
                                                }
                                            } else {
                                                Log.d("TAG", "Error: ", task.exception)
                                            }
                                        }
                                    }
                                Log.d("TAG", "Document doesn't exist.")
                            }
                        }
                    } else {
                        Log.d("TAG", "Error: ", task.exception)
                    }
                }
            }
        }

        documentReference.addSnapshotListener{ value, error ->
            if(error != null){
                Log.d("Error", "Unable to fetch data")
            }
            else{
                if(value!=null) {
                    messageArrayList.clear()

                    val messages = value.get("message") as? ArrayList<Map<String, Any>>

                    messages?.forEach { msg ->
                        messageArrayList.add(
                            MessageData(
                                msg["message"].toString(),
                                msg["senderId"].toString(), msg["timeStamp"] as Long
                            )
                        )

                    }

                    messageAdapter = MessagesAdapter(activity!!, messageArrayList)

                    binding?.chatRecyclerView?.adapter = messageAdapter

                    binding?.chatRecyclerView?.setHasFixedSize(true)
                    if(messageArrayList.size!=0) {
                        binding?.chatRecyclerView?.smoothScrollToPosition(messageArrayList.size - 1)
                    }

                }
            }
        }



        return view
    }

}