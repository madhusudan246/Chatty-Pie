package com.example.chatty.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatty.R
import com.example.chatty.databinding.ActivityAuthenticationBinding
import com.example.chatty.fragments.Login
import com.example.chatty.fragments.SignUp
import com.google.android.material.tabs.TabLayoutMediator

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var appAdapt: AuthPagerAdapter
    private var titles = arrayListOf("Login", "Sign Up")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        val view = binding.root
        setTheme(R.style.Theme_Chatty)
        setContentView(view)
        appAdapt = AuthPagerAdapter(this)
        binding.viewPager2MainAuth.adapter = appAdapt
        TabLayoutMediator(binding.tabLayoutMainAuth, binding.viewPager2MainAuth){
            tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    class AuthPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position)
            {
                0 -> Login()
                1 -> SignUp()
                else -> Login()
            }
        }

    }
}