package com.example.chatty.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatty.R
import com.example.chatty.adapters.MessagesAdapter
import com.example.chatty.databinding.FragmentMessageBinding
import com.example.chatty.modals.MessageData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.Date

class Message : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding
    private lateinit var toolbarMessage: androidx.appcompat.widget.Toolbar
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageArrayList: ArrayList<MessageData>
    private lateinit var fStore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var contentType: String
    private lateinit var image: ByteArray
    private lateinit var senderUid: String
    private lateinit var recieverUid: String

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        val view = binding?.root

        messageArrayList = arrayListOf()
        fStore = FirebaseFirestore.getInstance()

        recieverUid = activity?.intent?.getStringExtra("Uid").toString()
        val recieverName = activity?.intent?.getStringExtra("Name")
        val recieverImg = activity?.intent?.getStringExtra("RecieverProfilePic")

        senderUid = FirebaseAuth.getInstance().uid.toString()

        contentType = "Text"

        binding?.userName?.text = recieverName
        Picasso.get().load(recieverImg).placeholder(R.drawable.no_profile).error(R.drawable.no_profile).into(binding?.userImg)

        val activity = this.activity
        if (activity != null) {
            toolbarMessage=activity.findViewById(R.id.toolbarMenu)
        }

        toolbarMessage.visibility = View.GONE

        binding?.backArrowMessage?.drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.grey))

        binding?.chatMessage?.hint = "Message..."

        binding?.backArrowMessage?.setOnClickListener {
            binding?.backArrowMessage?.drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.splash))
            activity?.finish()
        }

        binding?.uploadImg?.setOnClickListener {
            showDialog()
        }

        val documentRef = fStore.collection("users")
            .document(senderUid)
            .collection("Chats")
            .document(recieverUid)


        documentReference = fStore.collection("users")
            .document(senderUid)
            .collection("Chats")
            .document(recieverUid)


        binding?.sendBtn?.setOnClickListener {
            if(binding?.chatMessage?.text.toString()!="") {
                contentType = "Text"

                val messageTxt = binding?.chatMessage?.text
                binding?.chatMessage?.setText("")


                val date = Date()
                val message = MessageData(messageTxt.toString(), senderUid, date.time, contentType, "")
                val msg = hashMapOf(
                    "message" to arrayListOf(message),
                )

                documentRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            if (document.exists()) {

                                documentRef.update("message", FieldValue.arrayUnion(message))
                                    .addOnSuccessListener {
                                        val documentRef2 = fStore.collection("users")
                                            .document(recieverUid)
                                            .collection("Chats")
                                            .document(senderUid)

                                        documentRef2.get().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val document2 = task.result
                                                if (document2 != null) {
                                                    if (document2.exists()) {

                                                        documentRef2.update(
                                                            "message",
                                                            FieldValue.arrayUnion(message)
                                                        )
                                                        Log.d("TAG", "Document already exists.")
                                                    } else {

                                                        documentRef2.set(msg)
                                                        Log.d("TAG", "Document doesn't exist.")
                                                    }
                                                }
                                            } else {
                                                Log.d("TAG", "Error: ", task.exception)
                                            }
                                        }
                                    }
                                Log.d("TAG", "Document already exists.")
                            } else {

                                documentRef.set(msg)
                                    .addOnSuccessListener {
                                        val documentRef2 = fStore.collection("users")
                                            .document(recieverUid)
                                            .collection("Chats")
                                            .document(senderUid)

                                        documentRef2.get().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val document2 = task.result
                                                if (document2 != null) {
                                                    if (document2.exists()) {
                                                        documentRef2.update(
                                                            "message",
                                                            FieldValue.arrayUnion(message)
                                                        )
                                                        Log.d("TAG", "Document already exists.")
                                                    } else {

                                                        documentRef2.set(msg)
                                                        Log.d("TAG", "Document doesn't exist.")
                                                    }
                                                }
                                            } else {
                                                Log.d("TAG", "Error: ", task.exception)
                                            }
                                        }
                                    }
                                Log.d("TAG", "Document doesn't exist.")
                            }
                        }
                    } else {
                        Log.d("TAG", "Error: ", task.exception)
                    }
                }
            }
        }

        documentReference.addSnapshotListener{ value, error ->
            if(error != null){
                Log.d("Error", "Unable to fetch data")
            }
            else{
                if(value!=null) {
                    messageArrayList.clear()

                    val messages = value.get("message") as? ArrayList<Map<String, Any>>

                    messages?.forEach { msg ->
                        messageArrayList.add(
                            MessageData(
                                msg["message"].toString(),
                                msg["senderId"].toString(), msg["timeStamp"] as Long,
                                msg["contentType"].toString(),
                                msg["caption"].toString()
                            )
                        )

                    }

                    messageAdapter = MessagesAdapter(activity!!, messageArrayList)

                    binding?.chatRecyclerView?.adapter = messageAdapter

                    binding?.chatRecyclerView?.setHasFixedSize(true)
                    if(messageArrayList.size!=0) {
                        binding?.chatRecyclerView?.smoothScrollToPosition(messageArrayList.size - 1)
                    }

                }
            }
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
            galleryCheckPermission()
            Toast.makeText(requireContext(), "Gallery Button", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    private fun galleryCheckPermission() {
        Dexter.withContext(requireContext())
            .withPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(
                object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        gallery()
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(requireActivity(), "You have denied the storage permission to select image", Toast.LENGTH_SHORT).show()

                        showRotationalDialogForPermission()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        showRotationalDialogForPermission()
                    }

                }
            )
            .onSameThread().check()
    }

    private fun gallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun cameraCheckPermission() {
        Dexter.withContext(requireContext())
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .withListener(
                object : MultiplePermissionsListener {
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

                    showImageDialog(bitmap, resultCode)
                }
                GALLERY_REQUEST_CODE -> {
                    if (data != null) {
                        if (data.data != null) {
                            val pfd = context?.contentResolver?.openFileDescriptor(data.data!!, "r")
                            val bitmap =
                                BitmapFactory.decodeFileDescriptor(pfd?.fileDescriptor, null, null)
                            pfd?.close()

                            showImageDialog(bitmap, resultCode)

                        }
                    }
                }
            }
        }
    }

    private fun showImageDialog(bitmap: Bitmap, code: Int) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.message_image_viewer)

        val captionMessage: TextInputEditText = dialog.findViewById(R.id.captionMessage)
        val selectedImg: ImageView = dialog.findViewById(R.id.selectedImage)
        val sendBtn: FloatingActionButton = dialog.findViewById(R.id.sendImgBtn)

        captionMessage.hint = "Caption here..."

        Glide.with(requireContext())
            .load(bitmap)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(selectedImg)

        sendBtn.setOnClickListener {
            contentType = "Image"
            dialog.dismiss()

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            image = byteArrayOutputStream.toByteArray()

            val currDate = Date().time

            // Process the selected media URI
            val reference = FirebaseStorage.getInstance().reference.child("$senderUid/$recieverUid/Message")
                .child(currDate.toString())

            reference.putBytes(image)
                .addOnSuccessListener { uploadTask ->
                    uploadTask.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        Log.d("Status", "Image uploaded successfully. Download URL: $downloadUrl")

                        val captionTxt = captionMessage.text.toString()
                        captionMessage.setText("")

                        val message = MessageData(
                            downloadUrl.toString(),
                            senderUid,
                            currDate,
                            contentType,
                            captionTxt
                        )
                        message.setCaption(captionTxt)
                        val msg = hashMapOf(
                            "message" to arrayListOf(message),
                        )

                        documentReference.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val document = task.result
                                if (document != null) {
                                    if (document.exists()) {

                                        documentReference.update("message", FieldValue.arrayUnion(message))
                                            .addOnSuccessListener {
                                                val documentRef2 = fStore.collection("users")
                                                    .document(recieverUid)
                                                    .collection("Chats")
                                                    .document(senderUid)

                                                documentRef2.get().addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val document2 = task.result
                                                        if (document2 != null) {
                                                            if (document2.exists()) {

                                                                documentRef2.update(
                                                                    "message",
                                                                    FieldValue.arrayUnion(message)
                                                                )
                                                                Log.d("TAG", "Document already exists.")
                                                            } else {

                                                                documentRef2.set(msg)
                                                                Log.d("TAG", "Document doesn't exist.")
                                                            }
                                                        }
                                                    } else {
                                                        Log.d("TAG", "Error: ", task.exception)
                                                    }
                                                }
                                            }
                                        Log.d("TAG", "Document already exists.")
                                    } else {

                                        documentReference.set(msg)
                                            .addOnSuccessListener {
                                                val documentRef2 = fStore.collection("users")
                                                    .document(recieverUid)
                                                    .collection("Chats")
                                                    .document(senderUid)

                                                documentRef2.get().addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val document2 = task.result
                                                        if (document2 != null) {
                                                            if (document2.exists()) {
                                                                documentRef2.update(
                                                                    "message",
                                                                    FieldValue.arrayUnion(message)
                                                                )
                                                                Log.d("TAG", "Document already exists.")
                                                            } else {

                                                                documentRef2.set(msg)
                                                                Log.d("TAG", "Document doesn't exist.")
                                                            }
                                                        }
                                                    } else {
                                                        Log.d("TAG", "Error: ", task.exception)
                                                    }
                                                }
                                            }
                                        Log.d("TAG", "Document doesn't exist.")
                                    }
                                }
                            } else {
                                Log.d("TAG", "Error: ", task.exception)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Status", "Failed to upload media", exception)
                }
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

//    private fun uriToCompressedBitmap(context: Context, uri: Uri): ByteArray {
//        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
//        val bitmap =
//            BitmapFactory.decodeFileDescriptor(pfd?.fileDescriptor, null, null)
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
//        pfd?.close()
//        return baos.toByteArray()
//    }

}