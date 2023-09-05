package com.example.anew.view

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
import com.example.anew.adapter.OnProfileImageClickListener
import com.example.anew.databinding.FragmentHomeBinding
import com.example.anew.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
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

        setupBottomNavigationView()
        setupButtonClick()

        auth.uid?.let {

            fetchUserData(auth.uid!!)

            viewModel.fetchPosts()


            postsAdapter = HomePostsAdapter(emptyList(), object : OnProfileImageClickListener{
                override fun onProfileImageClick(senderId: String) {
                    val fragment = ProfileViewerFragment.newInstance(senderId)
                    Log.e("senderidHomeFragmentToVieweFragment", senderId)
                    if(senderId == auth.uid){
                        findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    }else{
                        findNavController().navigate(R.id.action_homeFragment_to_profileViewerFragment,
                            bundleOf("senderId" to senderId))
                    }

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


    private fun setupBottomNavigationView(){
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {

            when(it.itemId){

                R.id.ic_action_search -> {
                    Log.e("search","click")
                    findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
                    false
                }
                R.id.ic_action_notification -> {
                    Log.e("notification","click")
                    findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment)
                    false
                }
                R.id.ic_action_inbox -> {
                    Log.e("inbox","click")
                    findNavController().navigate(R.id.action_homeFragment_to_inboxFragment)
                    false
                }

                else -> false
            }
        }
        val menuItem = binding.bottomNavigationView.menu.getItem(0)
        menuItem.isChecked = true
    }
    private fun setupButtonClick(){
        binding.circleImage.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
        binding.buttonAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_shareFragment)
        }
        binding.textViewFollowers.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_homeFollowersFragment)
        }
    }
    private fun fetchUserData(userId: String) {

        try {
            viewModel.fetchUserData(userId)
        } catch (e: Exception) {
            Log.e("homeFragmentFetchUserdata", e.message.toString())
        }


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
