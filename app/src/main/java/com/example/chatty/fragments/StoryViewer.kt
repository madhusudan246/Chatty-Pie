package com.example.chatty.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.chatty.R
import com.example.chatty.databinding.FragmentStoryViewerBinding
import jp.shts.android.storiesprogressview.StoriesProgressView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs


class StoryViewer : Fragment(), StoriesProgressView.StoriesListener, GestureDetector.OnGestureListener {
    private var pressTime: Long = 0L
    private var limit: Long = 1000L
    private var _binding: FragmentStoryViewerBinding? = null
    private val binding get() = _binding

    private lateinit var resources: ArrayList<String>
    private lateinit var timeStampList: ArrayList<String>

    private lateinit var toolbarStatusViewer: androidx.appcompat.widget.Toolbar
    private lateinit var gestureDetector: GestureDetectorCompat

    private var counter = 0
    private var flag = true
    private var isPaused = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoryViewerBinding.inflate(inflater, container, false)
        val view = binding?.root

        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        gestureDetector = GestureDetectorCompat(requireContext(), this)

        val activity = this.activity
        if(activity!=null){
            toolbarStatusViewer = activity.findViewById(R.id.toolbarMenu)
        }

        toolbarStatusViewer.visibility = View.GONE

        resources = arrayListOf()
        timeStampList = arrayListOf()

        val name = activity?.intent?.getStringExtra("Name")
        val profilePhoto = activity?.intent?.getStringExtra("ProfilePhoto")
        val storiesArray = activity?.intent?.getSerializableExtra("stories") as? ArrayList<Map<String, Any>>

        binding?.userStatusName?.text = name
        Glide.with(requireContext())
            .load(profilePhoto)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .placeholder(R.drawable.no_profile)
            .error(R.drawable.no_profile)
            .into(binding?.userStatusProfile!!)

        for(story in storiesArray!!){
            resources.add(story["statusImageUri"] as String)

            val recentUpdate = story["lastUpdated"]

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
            timeStampList.add(formattedDate)
        }

        binding?.stories?.setStoriesCount(resources.size)
        binding?.stories?.setStoryDuration(5000L)
        binding?.stories?.setStoriesListener(this)

        loadImage(resources[counter])

        binding?.timeStampStatus?.text = timeStampList[counter]

        binding?.reverse?.setOnClickListener {
            onPrev()
//            binding?.stories?.reverse()
//            flag = false
//            it.setOnTouchListener(object : View.OnTouchListener {
//                    @SuppressLint("ClickableViewAccessibility")
//                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
////                        flag = false
//                        return !gestureDetector.onTouchEvent(event!!)
//                    }
//
//                })
        }

//        binding?.reverse?.setOnTouchListener(onTouchListener)

        binding?.skip?.setOnClickListener {
            onNext()
//            flag = true
//            it.setOnTouchListener(object : View.OnTouchListener {
//
//                @SuppressLint("ClickableViewAccessibility")
//                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
////                    flag = true
//                    return !gestureDetector.onTouchEvent(event!!)
//                }
//
//            })
////            binding?.stories?.skip()
        }


//        binding?.skip?.setOnTouchListener(object : View.OnTouchListener {
//
//            @SuppressLint("ClickableViewAccessibility")
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                flag = true
//                return !gestureDetector.onTouchEvent(event!!)
//            }
//
//        })


//        binding?.reverse?.setOnTouchListener(object : View.OnTouchListener {
//            @SuppressLint("ClickableViewAccessibility")
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                flag = false
//                return !gestureDetector.onTouchEvent(event!!)
//            }
//
//        })

        binding?.playPause?.setOnClickListener {
            if(!isPaused){
                Glide.with(requireContext())
                    .load(R.drawable.ic_play)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding?.playPause!!)
                binding?.stories?.pause()
                isPaused = true
            }
            else{
                Glide.with(requireContext())
                    .load(R.drawable.ic_pause)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding?.playPause!!)
                binding?.stories?.resume()
                isPaused = false
            }
        }

        binding?.flung?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return gestureDetector.onTouchEvent(event!!)
            }
        })

        return view
    }

    private fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
    }

    override fun onNext() {
        if(flag){
            counter++
            flag = false
        }
        Log.d("Counter Increment", "Incremented")
        if (counter < resources.size) {
            binding?.stories?.pause()
            loadImage(resources[counter])
            binding?.timeStampStatus?.text = timeStampList[counter]
        }
        else{
            requireActivity().finish()
        }
    }

    override fun onPrev() {
        if (counter > 0) {
            counter--
        }
        else{
            counter = 0
        }

        loadImage(resources[counter])
        binding?.timeStampStatus?.text = timeStampList[counter]
    }

    override fun onComplete() {
        requireActivity().finish()
    }

    override fun onDestroyView() {
        binding?.stories?.destroy()
        super.onDestroyView()
    }

    override fun onDown(event: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
        //not used
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        if (flag) {
            Toast.makeText(context, "Skipped", Toast.LENGTH_SHORT).show()
//            binding?.stories?.destroy()
            onNext()
        }

        if(!flag){
            Toast.makeText(context, "Previous", Toast.LENGTH_SHORT).show()
//            binding?.stories?.destroy()
            onPrev()
        }
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        //not used
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        //not used
    }

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        if (event2.y - event1.y > SWIPE_MIN_DISTANCE && abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            // Swiped from top to bottom (bottom to top is ignored in this case)
            requireActivity().finish()
            val slideFromUpAnimation = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_bottom)
            val rootView = requireActivity().findViewById<View>(android.R.id.content)
            rootView.startAnimation(slideFromUpAnimation)

        }
        return true
    }

    private fun loadImage(url: String) {

        Glide.with(requireContext())
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    startProgressView()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    startProgressView()
                    return false
                }

            })
            .into(binding?.image!!)
    }

    private fun startProgressView() {
        binding?.stories?.destroy()
        Log.d("Counter", "$counter")
        binding?.stories?.startStories(counter)
        flag = true
    }

    companion object{
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200
    }

}
