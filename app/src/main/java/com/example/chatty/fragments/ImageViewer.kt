package com.example.chatty.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatty.R
import com.example.chatty.databinding.FragmentImageViewerBinding
import kotlin.math.abs


class ImageViewer : Fragment(), GestureDetector.OnGestureListener {

    private var _binding: FragmentImageViewerBinding? = null
    private val binding get() =_binding
    private lateinit var toolbarMessage: androidx.appcompat.widget.Toolbar
    private lateinit var gestureDetector: GestureDetectorCompat

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentImageViewerBinding.inflate(inflater, container, false)
        val view = binding?.root

        val activity = this.activity
        if (activity != null) {
            toolbarMessage=activity.findViewById(R.id.toolbarMenu)
        }

        toolbarMessage.visibility = View.GONE

        gestureDetector = GestureDetectorCompat(requireContext(), this)

        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val caption = activity?.intent?.getStringExtra("Caption")
        val imageUrl = activity?.intent?.getStringExtra("ImageURL")

        if(caption == ""){
            binding?.imageCaption?.visibility = View.GONE
        }

        binding?.imageCaption?.text = ellipsize(caption)

        if(caption?.length!! >= 100){
            binding?.extendableText?.visibility = View.VISIBLE
        }

        binding?.extendableText?.setOnClickListener {
            binding?.imageCaption?.text = caption
            binding?.extendableText?.visibility = View.GONE
        }

        binding?.imageCaption?.movementMethod = ScrollingMovementMethod()

        binding?.fullSizeImage?.let {
            Glide.with(requireContext())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(it)
        }

        binding?.flung?.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event!!) }

        return view
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
        //not used
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
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

    private fun ellipsize(input: String?): String? {
        return if (input == null || input.length < 100) {
            input
        } else input.substring(0, 100) + "..."
    }

    companion object{
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200
    }
}