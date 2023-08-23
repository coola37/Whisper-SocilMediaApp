package com.example.anew.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.databinding.FragmentShareBinding
import com.example.anew.model.Posts
import com.example.anew.viewmodel.ShareViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@AndroidEntryPoint
class ShareFragment : Fragment(R.layout.fragment_share) {

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var db: FirebaseFirestore
    @Inject
    lateinit var glide: RequestManager
    @Inject
    lateinit var storageRef: FirebaseStorage

    private lateinit var binding: FragmentShareBinding
    private lateinit var viewModel: ShareViewModel
    private var selectedImageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShareBinding.bind(view)
        viewModel = ViewModelProvider(this)[ShareViewModel::class.java]

        fetchUserData(auth.uid!!)
        setupButtonsClick()


    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
        }
    }

    private fun fetchUserData(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.fetchUserData(userId)
            } catch (e: Exception) {
                Log.e("ShareFetchData", e.message.toString())
            }
        }

        viewModel.userData.observe(viewLifecycleOwner) { user ->
            glide.load(user.details?.profileImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_none_img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.circleProfile)
        }
    }

    private fun savePostToDb(post: Posts) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userData.observe(viewLifecycleOwner) { user ->
                post.postID = UUID.randomUUID().toString()
                post.date = SimpleDateFormat("dd/M/yyyy hh:mm").format(Date())
                post.senderID = user.userId
                post.senderUsername = user.username
                post.senderName = user.details?.name
                post.senderImg = user.details?.profileImg
                post.text = binding.editTextPost.text.toString()
                post.like = 0

                val imageUrl = selectedImageUri?.let { uri ->
                    try {
                        lifecycleScope.launch {
                            post.postImg = uploadImageToFirebaseStorage(uri)
                            viewModel.savePostToDb(post)

                        }
                    } catch (e: Exception) {
                        Log.e("ShareSaveData", e.message.toString())
                        ""
                    }
                } ?: ""

                post.postImg =  " "
                lifecycleScope.launch { viewModel.savePostToDb(post) }
                findNavController().navigate(R.id.action_shareFragment_to_homeFragment)

            }
        }
    }

    private suspend fun uploadImageToFirebaseStorage(uri: Uri): String {
        val imageRef = storageRef.reference.child("images/${UUID.randomUUID()}")

        return suspendCancellableCoroutine { continuation ->
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    continuation.resume(downloadUrl.toString()) {
                        uploadTask.cancel()
                    }
                }.addOnFailureListener { urlException ->
                    continuation.resumeWithException(urlException)
                }
            }.addOnFailureListener { uploadException ->
                continuation.resumeWithException(uploadException)
            }

            continuation.invokeOnCancellation {
                uploadTask.cancel()
            }
        }
    }
    private fun setupButtonsClick(){
        binding.imageViewClose.setOnClickListener {
            findNavController().navigate(R.id.action_shareFragment_to_homeFragment)
            requireActivity().finish()
        }


        binding.imageViewGalery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(intent)
        }

        binding.buttonShare.setOnClickListener {
            val post = Posts()
            selectedImageUri?.let { uri ->
                post.postImg = uri.toString()
            } ?: run {
                post.text = binding.editTextPost.text.toString()
            }
            savePostToDb(post)
        }
    }
}

