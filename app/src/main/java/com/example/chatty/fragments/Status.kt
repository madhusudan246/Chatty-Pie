package com.example.chatty.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import coil.load
import coil.transform.CircleCropTransformation
import com.example.chatty.R
import com.example.chatty.databinding.FragmentStatusBinding
import com.example.chatty.modals.StoriesData
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.Date

class Status : Fragment() {

    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var reference: StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var userId: String
    private lateinit var selectedMediaUri: Uri
    private lateinit var documentReference: DocumentReference
    private lateinit var image: ByteArray
    private val CAMERA_REQUEST_CODE = 1

    companion object {
        private const val REQUEST_CODE = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatusBinding.inflate(inflater, container, false)
        val view = binding?.root

        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        userId = fAuth.currentUser?.uid.toString()

        documentReference = fStore.collection("users")
            .document(userId)
            .collection("Status")
            .document("Stories")


        fStore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { task ->
                if(task!=null){
                    if(task.exists()){
                        val profileUrl = task.get("userProfilePhoto").toString()
                        val userName = task.get("Name").toString()

                        binding?.userStatusName?.text = userName

                        Picasso.get().load(profileUrl).placeholder(R.drawable.no_profile).error(R.drawable.no_profile).into(binding?.userStatusProfile)
                    }
                }
            }

        binding?.addStatusBtn?.setOnClickListener {
            showDialog()
        }


        return view
    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_dialog)

        val captureBtn: ShapeableImageView = dialog.findViewById(R.id.capture_camera_btn)
        val galleryBtn: ShapeableImageView = dialog.findViewById(R.id.gallery_btn_choose)

        captureBtn.setOnClickListener {
            dialog.dismiss()
            cameraCheckPermission()
            Toast.makeText(requireContext(), "Capture Button", Toast.LENGTH_SHORT).show()
        }
        galleryBtn.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Gallery Button", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    private fun cameraCheckPermission() {
        Dexter.withContext(requireContext())
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .withListener(
                object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if(report.areAllPermissionsGranted()){
                                camera()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRotationalDialogForPermission()
                    }

                }
            ).onSameThread().check()
    }

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(requireActivity())
            .setMessage("It looks like you have turned off permissions required for this feature. It can be enabled under App Settings")
            .setPositiveButton("Go to SETTINGS"){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    startActivity(intent)

                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun camera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                CAMERA_REQUEST_CODE -> {

                    val bitmap = data?.extras?.get("data") as Bitmap

//                    binding?.userStatusProfile?.load(bitmap){
//                        crossfade(true)
//                        crossfade(1000)
//                        transformations(CircleCropTransformation())
//                    }
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    image = byteArrayOutputStream.toByteArray()

                    val date = Date()
                    val currDate = date.time
                    // Process the selected media URI
                    reference = storage.reference.child("$userId/Status")
                        .child(currDate.toString())

                    reference.putBytes(image)
                        .addOnSuccessListener { uploadTask ->
                            uploadTask.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                    Log.d("Status", "Image uploaded successfully. Download URL: $downloadUrl")

                                    val stories = StoriesData(downloadUrl.toString(), currDate)
                                    val status = hashMapOf(
                                        "stories" to arrayListOf(stories)
                                    )

                                    documentReference.get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            if (documentSnapshot.exists()) {
                                                documentReference.update("stories", FieldValue.arrayUnion(stories))
                                                    .addOnSuccessListener {
                                                        Log.d("Status", "Status Updated Successfully")
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.e("Status", "Failed to update status", exception)
                                                    }
                                            } else {
                                                documentReference.set(status)
                                                    .addOnSuccessListener {
                                                        Log.d("Status", "Status Set Successfully")
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.e("Status", "Failed to set status", exception)
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("Status", "Failed to get document", exception)
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Status", "Failed to upload media", exception)
                            }
                    }
                }
            }
        }

//        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            // Handle the selected or captured media here
//            if (data != null) {
//                if (data.data != null) {
//                    val selectedMediaUri = data.data
//                    val date = Date()
//                    val currDate = date.time
//                    // Process the selected media URI
//                    reference = storage.reference.child("$userId/Status")
//                        .child(currDate.toString())
//
//                    reference.putFile(selectedMediaUri!!)
//                        .addOnSuccessListener { uploadTask ->
//                            uploadTask.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
//                                Log.d("Status", "Image uploaded successfully. Download URL: $downloadUrl")
//
//                                val stories = StoriesData(downloadUrl.toString(), currDate)
//                                val status = hashMapOf(
//                                    "stories" to arrayListOf(stories)
//                                )
//
//                                documentReference.get()
//                                    .addOnSuccessListener { documentSnapshot ->
//                                        if (documentSnapshot.exists()) {
//                                            documentReference.update("stories", FieldValue.arrayUnion(stories))
//                                                .addOnSuccessListener {
//                                                    Log.d("Status", "Status Updated Successfully")
//                                                }
//                                                .addOnFailureListener { exception ->
//                                                    Log.e("Status", "Failed to update status", exception)
//                                                }
//                                        } else {
//                                            documentReference.set(status)
//                                                .addOnSuccessListener {
//                                                    Log.d("Status", "Status Set Successfully")
//                                                }
//                                                .addOnFailureListener { exception ->
//                                                    Log.e("Status", "Failed to set status", exception)
//                                                }
//                                        }
//                                    }
//                                    .addOnFailureListener { exception ->
//                                        Log.e("Status", "Failed to get document", exception)
//                                    }
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            Log.e("Status", "Failed to upload media", exception)
//                        }
//                }
//            }
//        }
    }
