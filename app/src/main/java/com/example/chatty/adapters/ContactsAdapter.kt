package com.example.chatty.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.chatty.R
import com.example.chatty.activities.MenuActivity
import com.example.chatty.modals.UserListData
import com.squareup.picasso.Picasso

class ContactsAdapter(val context: Activity?, private var userList: ArrayList<UserListData>): RecyclerView.Adapter<ContactsAdapter.MyViewHolder> (){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.each_item, parent, false)
        Log.d("Adapter", "myHolderView set")
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("Holder", "Holder set")
        val user: UserListData = userList[position]
        val imageLoader = ImageLoader.Builder(context!!)
            .diskCachePolicy(CachePolicy.ENABLED) // Enable disk caching
            .build()

        holder.userName.text = user.Name
        holder.userAbout.text = user.About
        val request = ImageRequest.Builder(context)
            .data(user.userProfilePhoto)
            .crossfade(true)
            .crossfade(1000)
            .transformations(CircleCropTransformation())
            .placeholder(R.drawable.no_profile)
            .error(R.drawable.no_profile)
            .target(holder.userProfile)
            .build()

        imageLoader.enqueue(request)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MenuActivity::class.java)
            intent.putExtra("OptionName", "MessagingScreen")
            intent.putExtra("Uid", user.uid)
            intent.putExtra("Name", user.Name)
            intent.putExtra("RecieverProfilePic", user.userProfilePhoto)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setFilteredList(userList: ArrayList<UserListData>){
        this.userList = userList
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userProfile: ImageView = itemView.findViewById(R.id.user_pfp)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userAbout: TextView = itemView.findViewById(R.id.user_aboutMe)
    }
}