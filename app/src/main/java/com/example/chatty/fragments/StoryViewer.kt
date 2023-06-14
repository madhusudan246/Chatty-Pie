package com.example.chatty.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.chatty.R
import com.example.chatty.databinding.FragmentStoryViewerBinding
import jp.shts.android.storiesprogressview.StoriesProgressView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs


class StoryViewer : Fragment(), StoriesProgressView.StoriesListener, GestureDetector.OnGestureListener {
    private var pressTime: Long = 0L
    private var limit: Long = 500L
    private var _binding: FragmentStoryViewerBinding? = null
    private val binding get() = _binding

    private lateinit var resources: ArrayList<String>
    private lateinit var timeStampList: ArrayList<String>

    private lateinit var toolbarStatusViewer: androidx.appcompat.widget.Toolbar
    private lateinit var gestureDetector: GestureDetectorCompat

    private var counter = 0

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                binding?.stories?.pause()
                false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                binding?.stories?.resume()
                limit < now - pressTime
            }
            else -> false
        }
    }

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
        binding?.stories?.startStories(0)

        loadImage(resources[counter])

        binding?.timeStampStatus?.text = timeStampList[counter]

        binding?.reverse?.setOnClickListener {
            binding?.stories?.reverse()
        }

        binding?.reverse?.setOnTouchListener(onTouchListener)

        binding?.skip?.setOnClickListener {
            binding?.stories?.skip()
        }

        binding?.skip?.setOnTouchListener(onTouchListener)

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
        counter++
        if (counter < resources.size) {
            loadImage(resources[counter])
            binding?.timeStampStatus?.text = timeStampList[counter]
        }
    }

    override fun onPrev() {
        if (counter > 0) {
            counter--
            loadImage(resources[counter])
            binding?.timeStampStatus?.text = timeStampList[counter]
        }
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
        //not used
        return false
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
            Toast.makeText(context, "Flinged!!", Toast.LENGTH_SHORT).show()
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
        binding?.stories?.startStories(counter)
    }

    companion object{
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200
    }

}
