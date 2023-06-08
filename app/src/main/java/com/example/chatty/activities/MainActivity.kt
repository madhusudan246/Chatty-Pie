package com.example.chatty.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatty.R
import com.example.chatty.fragments.Calls
import com.example.chatty.fragments.Chats
import com.example.chatty.databinding.ActivityMainBinding
import com.example.chatty.fragments.Status
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appAdapt: AppPagerAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private val titles = arrayListOf("Chats", "Status", "Calls")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
//        val intent = Intent(this@MainActivity, AuthenticationActivity::class.java)
//        startActivity(intent)
//        finish()
        setTheme(R.style.Theme_Chatty)
        setContentView(view)
        window.statusBarColor = ContextCompat.getColor(this, R.color.greyTint)
        auth = Firebase.auth
        user = auth.currentUser!!

        binding.toolbarMain.title = "Chatty"
        setSupportActionBar(binding.toolbarMain)
        appAdapt = AppPagerAdapter(this)
        binding.viewPager2Main.adapter = appAdapt
        TabLayoutMediator(binding.tabLayoutMain, binding.viewPager2Main){
            tab, position ->
            tab.text = titles[position]
        }.attach()

        binding.newMessageBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, MenuActivity::class.java)
            intent.putExtra("OptionName", "Contacts")
            startActivity(intent)
        }

    }
    class AppPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position)
            {
                0 -> Chats()
                1 -> Status()
                2 -> Calls()
                else -> Chats()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.opt_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.LogOutOpt -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, AuthenticationActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.settings -> {
                val intent = Intent(this@MainActivity, MenuActivity::class.java)
                intent.putExtra("OptionName", "Settings")
                startActivity(intent)
            }
            R.id.aboutUs -> {
                val intent = Intent(this@MainActivity, MenuActivity::class.java)
                intent.putExtra("OptionName", "About")
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}