package com.example.anew.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.databinding.FragmentEditProfileBinding
import com.example.anew.model.Users
import com.example.anew.viewmodel.EditProfileViewModel
import com.example.anew.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    private val IMAGE_PICKER_REQUEST_CODE = 123
    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var db: FirebaseFirestore
    private lateinit var imgURİ: Uri
    @Inject
    lateinit var glide: RequestManager
    private var profileUrl: String = ""



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfileBinding.bind(view)
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
        imgURİ = Uri.EMPTY

        getUserData(auth.uid!!)
        setupButton()


    }

    private fun setupButton(){
        binding.textViewChangePhoto.setOnClickListener {
            openImagePicker()
        }
        binding.textViewSave.setOnClickListener {
            val name = binding.editTextTextName.text.toString()
            val bio = binding.editTextTextBio.text.toString()
            val selectedImageUri = imgURİ

            if (selectedImageUri != Uri.EMPTY) {
                lifecycleScope.launch {
                    val imageUrl = viewModel.uploadProfileImageAndGetUrl(auth.currentUser!!.uid, selectedImageUri)
                    viewModel.updateProfileData(auth.currentUser!!.uid, name, bio, imageUrl)
                    viewModel.updateSenderImgInPosts(auth.currentUser!!.uid, imageUrl, name)
                    viewModel.updateImgInChatChannel(auth.uid!!, imageUrl)
                    viewModel.updateProfileImgSenderChInChat(auth.uid!!, imageUrl)
                    viewModel.updateProfileImgRecevierChInChat(auth.uid!!, imageUrl)
                    Log.e("imageUrl", imageUrl)
                    this@EditProfileFragment.onDestroy()
                    findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)

                }
            }else{
                viewModel.updateProfileData(auth.currentUser!!.uid, name, bio, profileUrl)
                viewModel.updateSenderImgInPosts(auth.currentUser!!.uid, profileUrl, name)
                this@EditProfileFragment.onDestroy()
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            }
        }
        binding.imageViewBack.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data

            if (selectedImageUri != null) {
                    imgURİ = selectedImageUri
                    binding.profileImg.setImageURI(selectedImageUri)
            }
        }
    }

    private fun getUserData(userId :String){
        viewModel.fetchUserData(userId)

        viewModel.userData.observe(viewLifecycleOwner){
            binding.editTextTextBio.hint = it.details?.bio
            binding.editTextTextName.hint = it.details?.name
            profileUrl = it.details?.profileImg!!

            glide.load(it.details?.profileImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.profileImg)
        }
    }

}