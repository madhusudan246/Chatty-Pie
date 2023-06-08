package com.example.chatty.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatty.R
import com.example.chatty.databinding.ItemRecieveBinding
import com.example.chatty.databinding.ItemSendBinding
import com.example.chatty.modals.MessageData
import com.google.firebase.auth.FirebaseAuth

class MessagesAdapter(private val context: Context?,
                      private val messageData: ArrayList<MessageData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_SENT = 1
        const val ITEM_RECEIVE = 2
    }

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: ItemSendBinding = ItemSendBinding.bind(itemView)
    }

    class RecieveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: ItemRecieveBinding = ItemRecieveBinding.bind(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageData[position]
        return if(FirebaseAuth.getInstance().uid == message.getSenderId()){
            ITEM_SENT
        }else {
            ITEM_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("onCreateViewHolder", "Initialized")
        return when (viewType){
            ITEM_SENT -> SentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_send, parent, false))
            ITEM_RECEIVE -> RecieveViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recieve, parent, false))
            else -> throw IllegalArgumentException("Unknown view Type: $viewType")
        }
    }

    override fun getItemCount(): Int {
        println(messageData.size)
        return messageData.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageData[position]

        Log.d("onBindViewHolder", "Initialized")

        if(holder is SentViewHolder){
            holder.binding.senderMsg.text = message.getMessage()
        }
        else if(holder is RecieveViewHolder){
            holder.binding.recieverMsg.text = message.getMessage()
        }
    }

}