package com.example.chatty.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatty.activities.MainActivity
import com.example.chatty.R
import com.example.chatty.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onStart() {
        super.onStart()
        fAuth = Firebase.auth
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding?.root

        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = activity?.let { GoogleSignIn.getClient(it, googleSignInOption) }!!

        binding?.googleLogin?.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent,100)
        }

        binding?.loginBtn?.setOnClickListener {
            val email = binding?.enterLoginEmail?.text.toString()
            val pass = binding?.enterpassword?.text.toString()
            if(TextUtils.isEmpty(email)){
                binding?.enterLoginEmail?.error = "Please enter a valid Email"
            }
            else if(TextUtils.isEmpty(pass)){
                binding?.enterpassword?.error = "Please enter a valid Password"
            }
            else {
                binding?.progressBarLogin?.visibility=View.VISIBLE
                signIn(email, pass)
            }
        }

        return view
    }

    private fun signIn(email: String, pass: String) {
        fAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding?.progressBarLogin?.visibility = View.GONE
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Login Info", "signInWithEmail:success")
                    Toast.makeText(
                        activity,
                        "Logged In Successfully",
                        Toast.LENGTH_SHORT,
                    ).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    binding?.progressBarLogin?.visibility = View.GONE
                    // If sign in fails, display a message to the user.
                    Log.w("Login Info", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        activity,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100){
            val signInAccountTask: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            if(signInAccountTask.isSuccessful){
                Toast.makeText(activity, "Google Signed In Successfully", Toast.LENGTH_SHORT).show()

                try{
                    val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                    if(googleSignInAccount != null){
                        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                            googleSignInAccount.idToken, null
                        )

                        Log.d("mTag", "idToken = ${googleSignInAccount.idToken}")
                        activity?.let {
                            fAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(it) {task ->
                                    if(task.isSuccessful){
                                        fAuth.currentUser?.uid
                                        startActivity(Intent(activity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        activity?.finish()
                                        googleSignInClient.signOut()
                                        Toast.makeText(activity, "Firebase Authentication Successful", Toast.LENGTH_SHORT).show()
                                    }
                                    else{
                                        Toast.makeText(activity, "Firebase Authentication Failed: ${task.exception!!.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

}