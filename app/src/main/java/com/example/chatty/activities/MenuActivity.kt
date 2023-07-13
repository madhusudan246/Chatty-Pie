package com.example.chatty.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chatty.fragments.Message
import com.example.chatty.R
import com.example.chatty.fragments.About
import com.example.chatty.fragments.Contacts
import com.example.chatty.databinding.ActivityMenuBinding
import com.example.chatty.fragments.ImageViewer
import com.example.chatty.fragments.Settings
import com.example.chatty.fragments.StatusView
import com.example.chatty.fragments.StoryViewer

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var optionValue: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        val view = binding.root
        setTheme(R.style.Theme_Chatty)
        setContentView(view)
        window.statusBarColor = ContextCompat.getColor(this, R.color.greyTint)

        if(intent != null){
            optionValue = intent.getStringExtra("OptionName").toString()
            when (optionValue){
                "Settings" -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Settings()).commit()
                    binding.toolbarMenu.title = "Settings"
                }
                "MessagingScreen" -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Message()).commit()
                    binding.toolbarMenu.title = "Message"
                }
                "About" -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, About()).commit()
                    binding.toolbarMenu.title = "About Us"
                }
                "StatusScreen" -> {
//                    Toast.makeText(this, "Status Screen Called", Toast.LENGTH_SHORT).show()
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, StoryViewer()).commit()
                    binding.toolbarMenu.title = "Status View"
                }
                "Contacts" -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Contacts()).commit()
                    binding.toolbarMenu.title = "Contacts"
                }
                "ImageViewer" -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, ImageViewer()).commit()
                    binding.toolbarMenu.title = "ImageViewer"
                }
            }
        }
    }
}