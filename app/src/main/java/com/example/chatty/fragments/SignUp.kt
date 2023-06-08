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
import com.example.chatty.databinding.FragmentSignUpBinding
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class SignUp : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onStart() {
        super.onStart()
        fAuth = Firebase.auth
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding?.root

        fStore = FirebaseFirestore.getInstance()

        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = activity?.let { GoogleSignIn.getClient(it, googleSignInOption) }!!

        binding?.googleSignUp?.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent,100)
        }

        binding?.signUpBtn?.setOnClickListener {
            val email = binding?.enterSignupEmail?.text.toString()
            val pass = binding?.enterSignupPassword?.text.toString()
            val confirmPass = binding?.enterConfirmPassword?.text.toString()
            if(TextUtils.isEmpty(email)){
                binding?.enterSignupEmail?.error = "Please enter a valid Email"
            }
            else if(TextUtils.isEmpty(pass) || pass.length<6){
                binding?.enterSignupPassword?.error = "Please enter a valid Password"
            }
            else if(pass!=confirmPass) {
                binding?.enterConfirmPassword?.error = "Password does not match"
            }
            else {
                binding?.signUpProgress?.visibility = View.VISIBLE
                fAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding?.signUpProgress?.visibility = View.GONE
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Registered Info",  "createUserWithEmail:success")
                            Toast.makeText(
                                activity,
                                "Registered Successfully",
                                Toast.LENGTH_SHORT,
                            ).show()
                            val userInfo = fAuth.currentUser?.uid
                            documentReference = fStore.collection("users").document(userInfo.toString())
                            val user = hashMapOf(
                                "Email" to email,
                                "Password" to pass,
                                "Name" to "user_name",
                                "About" to "Hey, I'm using chatty pie",
                            )

                            documentReference.set(user).addOnSuccessListener {
                                Log.d("OnSuccess", "User created successfully")
                            }
                            val intent = Intent(activity, MainActivity::class.java)
                            startActivity(intent)
                            activity?.finish()

                        } else {
                            binding?.signUpProgress?.visibility = View.GONE
                            // If sign in fails, display a message to the user.
                            Log.w("Registered Info", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                activity,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [START initialize_auth]
        // Initialize Firebase Auth
        fAuth = Firebase.auth
        // [END initialize_auth]
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
                                        val userInfo = fAuth.currentUser?.uid
                                        val email = fAuth.currentUser?.email.toString()
                                        documentReference = fStore.collection("users").document(userInfo.toString())
                                        val user = hashMapOf(
                                            "Email" to email,
                                            "Password" to "",
                                            "Name" to "user_name",
                                            "About" to "Hey, I'm using chatty pie",
                                        )

                                        documentReference.set(user).addOnSuccessListener {
                                            Log.d("OnSuccess", "User created successfully")
                                        }
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