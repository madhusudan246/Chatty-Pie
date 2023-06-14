package com.example.chatty.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.chatty.R
import com.example.chatty.databinding.ItemStatusBinding
import com.example.chatty.modals.StoryData

class StoryAdapter(val context: Activity?, private var storyData: MutableList<StoryData>): RecyclerView.Adapter<StoryAdapter.MyViewHolder>(){

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding: ItemStatusBinding = ItemStatusBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return storyData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = storyData[position]

        holder.binding.userStatusProfile.load(story.UserProfilePhoto){
            crossfade(true)
            crossfade(1000)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.no_profile)
            error(R.drawable.no_profile)
        }
        holder.binding.storyImage.load(story.statusImageUrl){
            crossfade(true)
            crossfade(500)
            placeholder(R.drawable.ic_gallery)
            error(R.drawable.ic_gallery)
        }
        holder.binding.userStatusName.text = story.Name
        holder.binding.timeStampStatus.text = story.timeStamp

    }

}