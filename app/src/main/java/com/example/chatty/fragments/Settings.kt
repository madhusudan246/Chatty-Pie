package com.example.chatty.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.chatty.R
import com.example.chatty.databinding.FragmentSettingsBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso


class Settings : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding
    private lateinit var toolbarSettings: androidx.appcompat.widget.Toolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var dialog: MaterialAlertDialogBuilder
    private lateinit var userId: String
    private lateinit var pass: String
    private lateinit var reference: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding?.root


        auth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        userId = auth.currentUser?.uid!!


        binding?.backArrow?.drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.grey))

        documentReference = fStore.collection("users").document(userId)
        documentReference.addSnapshotListener{ value, error ->
            if(error != null){
                Log.d("Error", "Unable to fetch data")
            }
            else{
                binding?.userNameTxt?.text = value?.getString("Name")
                binding?.aboutTxt?.text = value?.getString("About")
                binding?.emailUser?.text = value?.getString("Email")
                pass = value?.getString("Password").toString()
                Picasso.get().load(value?.getString("userProfilePhoto")).placeholder(R.drawable.no_profile).error(R.drawable.no_profile).into(binding?.noProfile)
            }

        }
        dialog = context?.let { MaterialAlertDialogBuilder(it) }!!
            .setMessage("Updating Profile......")
            .setCancelable(false)

        val activity = this.activity
        if (activity != null) {
            toolbarSettings=activity.findViewById(R.id.toolbarMenu)
        }

        toolbarSettings.visibility = View.GONE

        binding?.addImage?.setOnClickListener {
            ImagePicker.with(this)
                .crop(1f, 1f)	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding?.backArrow?.setOnClickListener {
            binding?.backArrow?.drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.splash))
            activity?.finish()
        }

        showEditTextDialog()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data != null){
            if(data.data != null){
                selectedImg = data.data!!

                binding?.noProfile?.setImageURI(selectedImg)
                reference = storage.reference.child("$userId/profilePhoto")

                reference.putFile(selectedImg).addOnSuccessListener{
                    reference.downloadUrl.addOnSuccessListener {
                        val user = hashMapOf(
                            "userProfilePhoto" to it.toString(),
                        )
                        documentReference.update(user as Map<String, Any>).addOnSuccessListener {
                            Log.d("Success", "Data updated successfully")
                            Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun showEditTextDialog() {
        binding?.linearLayoutName?.setOnClickListener {
            val view1 = LayoutInflater.from(activity).inflate(R.layout.edit_text_dialog, null)
            val editText = view1.findViewById<TextInputEditText>(R.id.edit_text_dg)
            val text = binding?.userNameTxt?.text.toString()
            val dialog = activity?.let { it1 -> MaterialAlertDialogBuilder(it1) }
                ?.setTitle(
                    "Enter Your Name"
                )
                ?.setView(view1)
                ?.setPositiveButton(
                    "Save"
                ) { dialogInterface, i ->
                    binding?.userNameTxt?.text = editText.text.toString()
                    dialogInterface.dismiss()
                }
                ?.setNegativeButton("Cancel") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                ?.create()

            dialog?.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FF99CC00"))
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF99CC00"))
            }

            dialog?.show()
            dialog?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

            dialog?.setOnDismissListener{
                val user = hashMapOf(
                    "Name" to binding?.userNameTxt?.text.toString(),
                )
                documentReference.update(user as Map<String, Any>).addOnSuccessListener {
                    Log.d("Success", "Data updated successfully")
                }
            }

            editText.setText(text)
            editText.selectAll()
            editText.requestFocus()

        }

        binding?.linearLayoutAbout?.setOnClickListener {
            val view1 = LayoutInflater.from(activity).inflate(R.layout.edit_text_dialog, null)
            val editText = view1.findViewById<TextInputEditText>(R.id.edit_text_dg)
            val text = binding?.aboutTxt?.text.toString()
            val dialog = activity?.let { it1 -> MaterialAlertDialogBuilder(it1) }
                ?.setTitle(
                    "Tell Something About You"
                )
                ?.setView(view1)
                ?.setPositiveButton(
                    "Save"
                ) { dialogInterface, i ->
                    binding?.aboutTxt?.text = editText.text.toString()
                    dialogInterface.dismiss()
                }
                ?.setNegativeButton("Cancel") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                ?.create()

            dialog?.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FF99CC00"))
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF99CC00"))
            }

            dialog?.show()

            dialog?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

            dialog?.setOnDismissListener{
                val user = hashMapOf(
                    "About" to binding?.aboutTxt?.text.toString(),
                )
                documentReference.update(user as Map<String, Any>).addOnSuccessListener {
                    Log.d("Success", "Data updated successfully")
                }
            }

            editText.setText(text)
            editText.selectAll()
            editText.requestFocus()

        }


    }

}