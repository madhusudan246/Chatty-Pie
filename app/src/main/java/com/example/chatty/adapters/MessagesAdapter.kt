package com.example.chatty.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatty.R
import com.example.chatty.databinding.ItemRecieveBinding
import com.example.chatty.databinding.ItemRecieveImgBinding
import com.example.chatty.databinding.ItemSendBinding
import com.example.chatty.databinding.ItemSendImgBinding
import com.example.chatty.modals.MessageData
import com.google.firebase.auth.FirebaseAuth

class MessagesAdapter(private val context: Context?,
                      private val messageData: ArrayList<MessageData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_SENT = 1
        const val ITEM_RECEIVE = 2
        const val ITEM_SENT_IMAGE = 3
        const val ITEM_RECEIVE_IMAGE = 4
    }

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: ItemSendBinding = ItemSendBinding.bind(itemView)
    }

    class SentImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: ItemSendImgBinding = ItemSendImgBinding.bind(itemView)
    }

    class RecieveImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: ItemRecieveImgBinding = ItemRecieveImgBinding.bind(itemView)
    }

    class RecieveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: ItemRecieveBinding = ItemRecieveBinding.bind(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageData[position]
        var result = ITEM_SENT
        if(FirebaseAuth.getInstance().uid == message.getSenderId()){
            if (message.getContentType() == "Image") result = ITEM_SENT_IMAGE
            else result = ITEM_SENT
        }else {
            if (message.getContentType() == "Image") result = ITEM_RECEIVE_IMAGE
            else result = ITEM_RECEIVE
        }
        return result
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("onCreateViewHolder", "Initialized")
        return when (viewType){
            ITEM_SENT -> SentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_send, parent, false))
            ITEM_RECEIVE -> RecieveViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recieve, parent, false))
            ITEM_SENT_IMAGE -> SentImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_send_img, parent, false))
            ITEM_RECEIVE_IMAGE -> RecieveImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recieve_img, parent, false))
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
        else if(holder is SentImageViewHolder){
            holder.binding.senderMsg.text = message.getCaption()
            Log.d("Caption", message.getCaption())

            if(message.getCaption() == ""){
                holder.binding.senderMsg.visibility = View.GONE
            }

            if (context != null) {
                Glide.with(context)
                    .load(message.getMessage())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.binding.senderImage)
            }
        }
        else if(holder is RecieveImageViewHolder){
            holder.binding.recieverMsg.text = message.getCaption()
            Log.d("Caption", message.getCaption())

            if(message.getCaption() == ""){
                holder.binding.recieverMsg.visibility = View.GONE
            }

            if (context != null) {
                Glide.with(context)
                    .load(message.getMessage())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.binding.recieverImage)
            }
        }
    }

}