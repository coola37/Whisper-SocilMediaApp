package com.example.anew.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.databinding.FragmentProfileBinding
import com.example.anew.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    @Inject
    lateinit var glide : RequestManager
    @Inject
    lateinit var auth: FirebaseAuth
    private lateinit var profileViewModel: ProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding.textViewSignOut.setOnClickListener {
            auth.signOut()
        }
        fetchUserData(auth.uid.toString())
        binding.buttonProfileEdit.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

    }

    private fun fetchUserData(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                profileViewModel.fetchUserData(userId)
            } catch (e: Exception) {
                // Hata durumunu işle
            }
        }

        profileViewModel.userData.observe(viewLifecycleOwner) { user ->

            binding.textViewUsername.text = user?.username
            binding.textViewName.text = user.details?.name
            binding.textViewBio.text = user.details?.bio
            binding.textViewFollowed.text = user.details?.followed.toString()
            binding.textViewFollowers.text = user.details?.followers.toString()

            glide.load(user.details?.profileImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.circleImageView)
            }
    }
}