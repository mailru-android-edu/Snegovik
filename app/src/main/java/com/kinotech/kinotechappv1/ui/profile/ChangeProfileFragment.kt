package com.kinotech.kinotechappv1.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.kinotech.kinotechappv1.R
import com.kinotech.kinotechappv1.databinding.ChangeProfileBinding

class ChangeProfileFragment : Fragment() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var photoAcc: ImageView
    private lateinit var nickName: TextView
    private var storagePhoto: StorageReference? = null
    private var checker = ""
    private var userUrl = ""
    private var photoUri: Uri? = null
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    companion object {
        private const val REQUEST_CODE = 1
        private const val PERMISSION_CODE = 2
    }

    private lateinit var binding: ChangeProfileBinding
    //private lateinit var model: ProfileSharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        profileViewModel =
//            ViewModelProvider(this).get(ProfileViewModel::class.java)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storagePhoto = FirebaseStorage.getInstance().reference.child("Profile Photo")
        binding = ChangeProfileBinding.inflate(inflater, container, false)
        val photoRef =  user?.uid?.let { FirebaseDatabase.getInstance().reference.child("Users").child(it) }

        binding.apply {
            userInfo(changeName, root, changePhoto)
            }
        binding.changePhotoButton.setOnClickListener{
            checker = "clicked"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context?.let { it1 ->
                        PermissionChecker.checkSelfPermission(
                            it1,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    } ==
                    PackageManager.PERMISSION_DENIED) {

                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    openGallery()
                }
            } else {
                openGallery()
            }
        }

        binding.saveButton.setOnClickListener {
            if (checker == "clicked"){
                Log.d("photoUri", "onCreateView:$photoUri ")
                uploadPhotoAndInfo()
            }
            else{
                updateUserInfoOnly()
            }
            loadfragment()
        }

        binding.backBtnCh.setOnClickListener {
            loadfragment()
        }
        return binding.root
    }
    private fun userInfo(nickName : TextView, v: View, img : ImageView){
        val usersRef = user?.uid?.let { FirebaseDatabase.getInstance().reference.child("Users").child(it) }
        usersRef?.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot){
                nickName.text = p0.child("fullName").value.toString()
                Glide
                    .with(v.context)
                    .load(p0.child("photo").value.toString())
                    .into(img)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun updateUserInfoOnly() {
        val userMap = HashMap<String, Any?>()
        userMap["fullName"] = binding.changeName.text.toString().toLowerCase()
        val usersRef = user?.let { FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(it.uid)
            .updateChildren(userMap)
        }

    }

    private fun uploadPhotoAndInfo(){
        val userMap = HashMap<String, Any?>()
        val  fileRef = storagePhoto!!.child(firebaseUser!!.uid+ "jpg")
        fileRef.putFile(photoUri!!).addOnCompleteListener{
            fileRef.downloadUrl.addOnCompleteListener {
                userUrl = it.result.toString()
                Log.d("photoUri", "onCreateViewURL:$userUrl")
                userMap["fullName"] = binding.changeName.text.toString().toLowerCase()
                userMap["photo"] = userUrl
                val usersRef = user?.let { it1->
                    FirebaseDatabase.getInstance().reference
                        .child("Users")
                        .child(it1.uid)
                        .updateChildren(userMap)
                }
            }
        }

        Log.d("photoUri", "onCreateView:$fileRef ")

        Log.d("photoUri", "onCreateView:$userUrl ")




    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            photoUri = data?.data
            binding.changePhoto.setImageURI(photoUri)
//            prefs?.edit()?.putString("profilePic", uri.toString())?.apply()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        model = ViewModelProvider(requireActivity()).get(ProfileSharedViewModel::class.java)
//        model.getPhoto().observe(viewLifecycleOwner, {
//            binding.changePhoto.setImageURI(it)
////            binding.changeName.setText(it.toString())
//        })
////        model.getPhoto().observe(viewLifecycleOwner, {
////            binding.changePhoto.setImageURI(it)
////        })
//        binding.saveButton.setOnClickListener {
//            model.putPhoto(binding.changePhoto.drawable.toString().toUri())
//            loadfragment()
//            loadfragmentch(binding.changeName.text.toString())
//        }
    }

    private fun loadfragmentch(editTextInput: String) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            val bun = Bundle()
            val profilefragment = ProfileFragment()
            bun.putString("message", editTextInput)
            profilefragment.arguments = bun
            transaction.replace(R.id.container, profilefragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    private fun loadfragment() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(R.id.container, ProfileFragment())
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            PERMISSION_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] ==
//                    PackageManager.PERMISSION_GRANTED
//                ) {
//                    openGallery()
//                } else {
//                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

//




//    private fun updateUserInfoOnly(){
//        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)
//        val userMap = HashMap<String, Any>()
//        userMap["username"] = changeName.getText().toString().toLowerCase()
//    }


}
