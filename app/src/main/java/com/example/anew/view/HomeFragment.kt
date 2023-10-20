package com.example.anew.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.adapter.HomePostsAdapter
import com.example.anew.adapter.OnClickListenerCatchData
import com.example.anew.databinding.FragmentHomeBinding
import com.example.anew.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var db: FirebaseFirestore
    @Inject
    lateinit var glide: RequestManager
    private lateinit var viewModel: HomeViewModel
    private lateinit var postsAdapter: HomePostsAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]


        setupButtonClick()

        auth.uid?.let {

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.fetchUserData(it)
                viewModel.fetchPosts()
            }

            CoroutineScope(Dispatchers.Main).launch{
                fetchUserData()
                postsAdapter = HomePostsAdapter(emptyList(), object : OnClickListenerCatchData{
                    override fun onProfileImageClick(senderId: String) {
                        if(senderId == auth.uid){
                            val intent = Intent(requireContext(), ProfileActivity::class.java)
                            startActivity(intent)
                        }else{
                            findNavController().navigate(R.id.action_homeFragment_to_profileViewerFragment,
                                bundleOf("senderId" to senderId))
                        }

                    }
                }, object : OnClickListenerCatchData{
                    override fun onProfileImageClick(senderId: String) {
                        CoroutineScope(Dispatchers.IO).launch { viewModel.checkLike(senderId) }
                        viewModel.checkLike.observe(viewLifecycleOwner){
                            if(it){
                                viewModel.disLike(senderId)
                                CoroutineScope(Dispatchers.Main).launch {
                                    viewModel.refreshPostData()
                                    viewModel.postsData.observe(viewLifecycleOwner){
                                        postsAdapter.setData(it)
                                    }
                                }
                            }else{
                                viewModel.like(senderId)
                                CoroutineScope(Dispatchers.Main).launch {
                                    viewModel.refreshPostData()
                                    viewModel.postsData.observe(viewLifecycleOwner){
                                        postsAdapter.setData(it)
                                    }
                                }
                            }
                        }
                    }

                }, object : OnClickListenerCatchData{
                    override fun onProfileImageClick(senderId: String) {
                        findNavController().navigate(R.id.action_homeFragment_to_postViewerFragment,
                            bundleOf("postID" to senderId))
                    }
                })

                viewModel.postsData.observe(viewLifecycleOwner) { postsList ->
                    postsAdapter.setData(postsList)

                    val layoutManager = LinearLayoutManager(requireContext())
                    binding.homeRecycler.layoutManager = layoutManager
                    binding.homeRecycler.adapter = postsAdapter

                }
            }
        }

    }




    private fun setupButtonClick(){
        binding.circleImage.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.buttonAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_shareFragment)
        }
        binding.textViewFollowers.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_homeFollowersFragment)
        }
    }
    private fun fetchUserData() {

        viewModel.userData.observe(viewLifecycleOwner) { user ->
            glide.load(user.details?.profileImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_none_img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.circleImage)
        }
    }

}
