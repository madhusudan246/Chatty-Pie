package com.example.chatty.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.chatty.R
import com.example.chatty.databinding.StatusItemBinding
import com.example.chatty.modals.UserStatusData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatusAdapter(val context: Activity?, private var statusData: ArrayList<UserStatusData>): RecyclerView.Adapter<StatusAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding: StatusItemBinding = StatusItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d("OnCreateViewHolder", "Created Successfully")
        val itemView = LayoutInflater.from(context).inflate(R.layout.status_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        println(statusData.size)
        return statusData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val status = statusData[position]
        val documentReference = FirebaseFirestore.getInstance()
            .collection("users")
            .document(status.uid)
            .collection("Status")
            .document("Stories")

        documentReference.addSnapshotListener { value, error ->
            if(error != null){
                Log.d("Error", "Unable to fetch data")
            }
            else{
                if(value != null){
                    val stories = value.get("stories") as? ArrayList<Map<String, Any>>

                    val recentUpdate = stories?.last()?.get("lastUpdated")

                    val calendar = Calendar.getInstance()
                    if (recentUpdate != null) {
                        calendar.timeInMillis = recentUpdate as Long
                    }

                    val todayCalendar = Calendar.getInstance() // Today's calendar
                    val yesterdayCalendar = Calendar.getInstance() // Yesterday's calendar
                    yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1)

                    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())

                    val formattedDate = when {
                        isSameDay(calendar, todayCalendar) -> "Today, ${format.format(calendar.time)}"
                        isSameDay(calendar, yesterdayCalendar) -> "Yesterday, ${format.format(calendar.time)}"
                        else -> format.format(calendar.time) // Default format for other dates
                    }

                    holder.binding.timeStampStatus.text = formattedDate
                }
            }
        }

        holder.binding.userStatusName.text = status.Name
        holder.binding.userStatusProfile.load(status.userProfilePhoto){
                        crossfade(true)
                        crossfade(1000)
                        placeholder(R.drawable.no_profile)
                        error(R.drawable.no_profile)
                    }


    }

    private fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
    }

}