package com.example.chatty.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.chatty.R
import com.example.chatty.activities.MenuActivity
import com.example.chatty.databinding.RowConversationsBinding
import com.example.chatty.modals.ConversationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter(val context: Activity?, private  var conversationList: ArrayList<ConversationData>): RecyclerView.Adapter<ConversationAdapter.MyViewHolder> (){
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: RowConversationsBinding = RowConversationsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(context).inflate(R.layout.row_conversations, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = conversationList[position]

        val userId: String? = FirebaseAuth.getInstance().uid

        val imageLoader = ImageLoader.Builder(context!!)
            .diskCachePolicy(CachePolicy.ENABLED) // Enable disk caching
            .build()


        val documentReference = FirebaseFirestore.getInstance().collection("users")
            .document(userId.toString())
            .collection("Chats")
            .document(user.uid)

        documentReference.addSnapshotListener{ value, error ->
            if(error != null){
                Log.d("Error", "Unable to fetch data")
            }
            else{
                if(value!=null) {
                    val messages = value.get("message") as? ArrayList<Map<String, Any>>

                    val recentMsg = messages?.last()?.get("message").toString()
                    val recentMsgTime: Long? = messages?.last()?.get("timeStamp") as? Long
                    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())


                    holder.binding.recentMsg.text = recentMsg
                    if(recentMsgTime!=null) {
                        holder.binding.msgTimeStamp.text =
                            dateFormat.format(Date(recentMsgTime))
                    }

                }
            }
        }

        holder.binding.conversationUser.text = user.Name
        val request = ImageRequest.Builder(context)
            .data(user.userProfilePhoto)
            .crossfade(true)
            .crossfade(1000)
            .transformations(CircleCropTransformation())
            .placeholder(R.drawable.no_profile)
            .error(R.drawable.no_profile)
            .target(holder.binding.userProfilePicture)
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
}