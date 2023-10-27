package com.example.anew.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.adapter.HomePostsAdapter
import com.example.anew.adapter.OnClickListenerCatchData
import com.example.anew.databinding.FragmentHomeFollowersBinding
import com.example.anew.viewmodel.HomeFollowersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFollowersFragment : Fragment(R.layout.fragment_home_followers) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    @Inject
    lateinit var glide: RequestManager
    private lateinit var adapter: HomePostsAdapter
    private lateinit var binding: FragmentHomeFollowersBinding
    private lateinit var viewModel: HomeFollowersViewModel
    private var followedUserList: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeFollowersBinding.bind(view)
        viewModel = ViewModelProvider(this)[HomeFollowersViewModel::class.java]

        setupButtonClick()

       auth.currentUser?.let {
           CoroutineScope(Dispatchers.IO).launch {
               viewModel.fetchUserData(auth.uid!!)
           }
           viewModel.userData.observe(viewLifecycleOwner){
               followedUserList = it.details?.listFollow!!
               glide.load(it.details?.profileImg)
                   .placeholder(R.mipmap.ic_none_img)
                   .error(R.mipmap.ic_none_img)
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .centerCrop()
                   .into(binding.circleImage)

               CoroutineScope(Dispatchers.IO).launch {
                   viewModel.fetchPosts(followedUserList)
               }
           }


           adapter = HomePostsAdapter(emptyList(), object : OnClickListenerCatchData {
               override fun onProfileImageClick(senderId: String) {
                 profileNavigation(senderId)
               }
           }, object : OnClickListenerCatchData {
               override fun onProfileImageClick(senderId: String) {
                   CoroutineScope(Dispatchers.IO).launch { viewModel.checkLike(senderId) }
                   viewModel.checkLike.observe(viewLifecycleOwner){
                    likeDislke(it, senderId)
                   }
               }

           }, object : OnClickListenerCatchData {
               override fun onProfileImageClick(senderId: String) {
                   findNavController().navigate(R.id.action_homeFragment_to_postViewerFragment,
                       bundleOf("postID" to senderId)
                   )
               }
           })
       }
        getPostsData()

    }

    private fun profileNavigation(senderId: String){
        if(senderId == auth.uid){
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }else{
            findNavController().navigate(R.id.action_homeFragment_to_profileViewerFragment,
                bundleOf("senderId" to senderId)
            )
        }
    }

    private fun likeDislke(it: Boolean, postId:String) {
        if (it) {
            viewModel.disLike(postId)
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.refreshPostData(followedUserList)
                viewModel.postsData.observe(viewLifecycleOwner) {
                    adapter.setData(it)
                }
            }
        } else {
            viewModel.like(postId)
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.refreshPostData(followedUserList)
                viewModel.postsData.observe(viewLifecycleOwner) {
                    adapter.setData(it)
                }
            }
        }
    }

    private fun getPostsData() {
        viewModel.postsData.observe(viewLifecycleOwner) {
            adapter.setData(it)
            val layoutManager = LinearLayoutManager(requireContext())
            binding.homeRecycler.layoutManager = layoutManager
            binding.homeRecycler.adapter = adapter
        }
    }

    private fun setupButtonClick(){
        binding.circleImage.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.buttonAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFollowersFragment_to_shareFragment)
        }
        binding.textViewEveryone.setOnClickListener {
            findNavController().navigate(R.id.action_homeFollowersFragment_to_homeFragment)
        }
    }
}

