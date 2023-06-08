package com.example.chatty.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import com.example.chatty.adapters.ContactsAdapter
import com.example.chatty.databinding.FragmentContactsBinding
import com.example.chatty.modals.UserListData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class Contacts : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding
    private lateinit var userArrayList: ArrayList<UserListData>
    private lateinit var myAdapter: ContactsAdapter
    private lateinit var fStore: FirebaseFirestore
    private lateinit var fAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        val view = binding?.root

//        layoutManager = LinearLayoutManager(context as Activity)
        binding?.recyclerView?.setHasFixedSize(true)

        userArrayList = arrayListOf()

        fStore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()
        fStore.collection("users").orderBy("Name").get().addOnSuccessListener {
            if (!it.isEmpty) {
                val userList = it.documents
                for (i in userList) {
                    if (i.id != fAuth.currentUser?.uid) {
                        val user = UserListData(
                            i.getString("Name").toString(),
                            i.getString("About").toString(),
                            i.getString("userProfilePhoto").toString(),
                            i.id
                        )
                        userArrayList.add(user)
                        myAdapter = ContactsAdapter(context as? Activity, userArrayList)
                        binding?.recyclerView?.adapter = myAdapter

                    } else {
                        Log.d("onFound", "This is user Account")
                    }
                }
            }
        }


        binding?.searchView?.setOnQueryTextListener(object: OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })

        return view
    }

    private fun filterList(query: String?) {
        if(query!=null){
            val search: String = query.lowercase()
            val filteredList = ArrayList<UserListData>()
            for(i in userArrayList){
                if(i.Name?.lowercase(Locale.ROOT)?.contains(search) == true){
                    filteredList.add(i)
                }
            }

            if(filteredList.isEmpty()){
                binding?.recyclerView?.visibility = View.GONE
                Toast.makeText(context as Activity, "No Data Found", Toast.LENGTH_SHORT).show()
            }
            else{
                binding?.recyclerView?.visibility = View.VISIBLE
                myAdapter.setFilteredList(filteredList)
            }
        }
    }

}