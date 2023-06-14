package com.example.chatty.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.chatty.R
import com.example.chatty.adapters.StoryAdapter
import com.example.chatty.databinding.FragmentStatusViewBinding
import com.example.chatty.modals.StoryData

class StatusView : Fragment() {

    private var _binding: FragmentStatusViewBinding? = null
    private val binding get() = _binding

    private lateinit var toolbarStatusView: androidx.appcompat.widget.Toolbar
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var timer: CountDownTimer

    private val stories: MutableList<StoryData> = arrayListOf()

    private var currentPosition = 0
    private var isPaused = false

    companion object {
        private const val STORY_DURATION = 5000L // Duration of each story in milliseconds
        private const val TICK_INTERVAL = 100L // Interval for updating progress in milliseconds
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatusViewBinding.inflate(inflater, container, false)
        val view = binding?.root

        val name = activity?.intent?.getStringExtra("Name")
        val profilePhoto = activity?.intent?.getStringExtra("ProfilePhoto")
        val timeStamp = activity?.intent?.getStringExtra("TimeStamp")
        val storiesArray = activity?.intent?.getSerializableExtra("stories") as? ArrayList<Map<String, Any>>

        val activity = this.activity
        if(activity!=null){
            toolbarStatusView = activity.findViewById(R.id.toolbarMenu)
        }

        toolbarStatusView.visibility = View.GONE

        // Getting Story data for user
        for(story in storiesArray!!){
            stories.add(StoryData(story["statusImageUri"] as String,
                name.toString(), profilePhoto.toString(), timeStamp.toString(), 5000L))

            storyAdapter = StoryAdapter(requireActivity(), stories)
            binding?.statusViewPager?.adapter = storyAdapter
        }


        //start Story Playback
        startPlayback()

        binding?.statusViewPager?.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                resetTimer()
            }
        })

        (binding?.statusViewPager as View).setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> pausePlayback()
                MotionEvent.ACTION_UP -> {
                    if (!isPaused) {
                        resumePlayback()
                    } else {
                        val threshold = binding!!.statusViewPager.width / 4
                        if (event.x < threshold) {
                            showPreviousStory()
                        } else if (event.x > binding!!.statusViewPager.width - threshold) {
                            showNextStory()
                        } else {
                            resumePlayback()
                        }
                    }
                    val clickThreshold = ViewConfiguration.get(view!!.context).scaledTouchSlop
                    val isClick = event.x <= clickThreshold && event.y <= clickThreshold
                    if (isClick) {
                        view.performClick() // Call performClick for click event
                    }
                }
            }
            false
        }


        return view
    }

    private fun showPreviousStory() {
        if(currentPosition > 0){
            currentPosition--
            binding?.statusViewPager?.setCurrentItem(currentPosition, true)
        }
        else{
            activity?.finish()
        }
    }

    private fun resumePlayback() {
        startPlayback()
        isPaused = false
    }

    private fun pausePlayback() {
        timer.cancel()
        isPaused = true
    }

    private fun resetTimer() {
        timer.cancel()
        startPlayback()
    }

    private fun startPlayback() {
        timer = object: CountDownTimer(STORY_DURATION, TICK_INTERVAL){
            override fun onTick(p0: Long) {
                // Update progress bar or story duration indicator
            }

            override fun onFinish() {
                showNextStory()
            }

        }.start()
    }

    private fun showNextStory() {
        if(currentPosition < stories.size - 1){
            currentPosition++
            binding?.statusViewPager?.setCurrentItem(currentPosition, true)
        }
        else{
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        _binding = null
    }

}