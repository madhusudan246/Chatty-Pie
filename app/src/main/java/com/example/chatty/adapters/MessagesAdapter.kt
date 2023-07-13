package com.example.chatty.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatty.R
import com.example.chatty.activities.MenuActivity
import com.example.chatty.databinding.ItemRecieveBinding
import com.example.chatty.databinding.ItemRecieveImgBinding
import com.example.chatty.databinding.ItemSendBinding
import com.example.chatty.databinding.ItemSendImgBinding
import com.example.chatty.fragments.ImageViewer
import com.example.chatty.modals.MessageData
import com.google.firebase.auth.FirebaseAuth

class MessagesAdapter(private val context: Context?,
                      private val messageData: ArrayList<MessageData>, private val fragmentManager: FragmentManager): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        val result: Int = if(FirebaseAuth.getInstance().uid == message.getSenderId()){
            if (message.getContentType() == "Image") ITEM_SENT_IMAGE
            else ITEM_SENT
        }else {
            if (message.getContentType() == "Image") ITEM_RECEIVE_IMAGE
            else ITEM_RECEIVE
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

        val data: Array<String> = arrayOf(message.getCaption(), message.getMessage())

        when (holder) {
            is SentViewHolder -> {
                holder.binding.senderMsg.text = message.getMessage()
            }

            is RecieveViewHolder -> {
                holder.binding.recieverMsg.text = message.getMessage()
            }

            is SentImageViewHolder -> {
                holder.binding.extendableText.visibility = View.GONE

                holder.binding.senderMsg.text = ellipsize(message.getCaption())

                if(message.getCaption().length >= 100){
                    holder.binding.extendableText.visibility = View.VISIBLE
                }

                holder.binding.extendableText.setOnClickListener {
                    holder.binding.senderMsg.text = message.getCaption()
                    holder.binding.extendableText.visibility = View.GONE
                }

                if(message.getCaption() == ""){
                    holder.binding.senderMsg.visibility = View.GONE
                }

                if (context != null) {
                    Glide.with(context)
                        .load(message.getMessage())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(holder.binding.senderImage)
                }

                holder.binding.senderImage.setOnClickListener {
                    val intent = Intent(context, MenuActivity::class.java)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, holder.binding.senderImage, ViewCompat.getTransitionName(holder.binding.senderImage).toString())
                    intent.putExtra("OptionName", "ImageViewer")
                    intent.putExtra("Caption", message.getCaption())
                    intent.putExtra("ImageURL", message.getMessage())
                    context.startActivity(intent, options.toBundle())
                }
            }

            is RecieveImageViewHolder -> {
                holder.binding.extendableText.visibility = View.GONE

                holder.binding.recieverMsg.text = ellipsize(message.getCaption())

                if(message.getCaption().length >= 100){
                    holder.binding.extendableText.visibility = View.VISIBLE
                }

                holder.binding.extendableText.setOnClickListener {
                    Log.d("Caption Size", "${message.getCaption().length}")
                    holder.binding.recieverMsg.text = message.getCaption()
                    holder.binding.extendableText.visibility = View.GONE
                }

                if(message.getCaption() == ""){
                    holder.binding.recieverMsg.visibility = View.GONE
                }

                if (context != null) {
                    Glide.with(context)
                        .load(message.getMessage())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(holder.binding.recieverImage)
                }

                holder.binding.recieverImage.setOnClickListener {
                    val intent = Intent(context, MenuActivity::class.java)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, holder.binding.recieverImage, ViewCompat.getTransitionName(holder.binding.recieverImage).toString())
                    intent.putExtra("OptionName", "ImageViewer")
                    intent.putExtra("Caption", message.getCaption())
                    intent.putExtra("ImageURL", message.getMessage())
                    context.startActivity(intent, options.toBundle())

                }
            }
        }
    }

    private fun ellipsize(input: String?): String? {
        return if (input == null || input.length < 100) {
            input
        } else input.substring(0, 100) + "..."
    }

}