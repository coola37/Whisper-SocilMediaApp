package com.example.anew.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.adapter.HomePostsAdapter
import com.example.anew.adapter.OnProfileImageClickListener
import com.example.anew.databinding.FragmentProfileBinding
import com.example.anew.viewmodel.EditProfileViewModel
import com.example.anew.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private lateinit var adapter: HomePostsAdapter
    private lateinit var editProfileViewModel: EditProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        editProfileViewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]


        CoroutineScope(Dispatchers.IO).launch {
            profileViewModel.fetchUserData(auth.uid!!)
            profileViewModel.fetchPosts()
        }

        CoroutineScope(Dispatchers.Main).launch{
            getUserData()
            setupButtons()
            adapter = HomePostsAdapter(emptyList(), object : OnProfileImageClickListener{
                override fun onProfileImageClick(senderId: String) {

                }
            }, object : OnProfileImageClickListener{
                override fun onProfileImageClick(senderId: String) {

                }

            }, object : OnProfileImageClickListener{
                override fun onProfileImageClick(senderId: String) {
                    findNavController().navigate(R.id.action_homeFragment_to_postViewerFragment,
                        bundleOf("postID" to senderId)
                    )
                }
            })

            profileViewModel.postsData.observe(viewLifecycleOwner){
                adapter.setData(it)

                val layoutManager = LinearLayoutManager(requireContext())
                binding.profileRecycler.layoutManager = layoutManager
                binding.profileRecycler.adapter = adapter
            }
        }
    }


    private fun getUserData() {

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

    private fun setupButtons(){
        binding.textViewSignOut.setOnClickListener {
            auth.signOut()
        }
        binding.buttonEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        editProfileViewModel.checkUserupdate.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch {
                if(it){
                    profileViewModel.updateUserData(auth.uid!!)

                    val checkCase = profileViewModel.checkUpdateUserData.value

                    profileViewModel.userData.observe(viewLifecycleOwner){

                    }

                    editProfileViewModel.checkUserupdate.postValue(checkCase)
                }else{
                    Log.d("User Data", "Data is current")
                }
            }
        }
    }
}