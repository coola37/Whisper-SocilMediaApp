package com.example.anew.view

import android.os.Binder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.adapter.HomePostsAdapter
import com.example.anew.adapter.OnProfileImageClickListener
import com.example.anew.databinding.FragmentProfileViewerBinding
import com.example.anew.viewmodel.HomeViewModel
import com.example.anew.viewmodel.ProfileViewerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ProfileViewerFragment : Fragment(R.layout.fragment_profile_viewer) {

    companion object {
        fun newInstance(senderId: String) : ProfileViewerFragment {
            val fragment = ProfileViewerFragment()
            val args = Bundle()
            args.putString("senderId", senderId)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var db: FirebaseFirestore
    @Inject
    lateinit var glide: RequestManager
    private lateinit var viewModel: ProfileViewerViewModel
    private lateinit var binding : FragmentProfileViewerBinding
    private lateinit var adapter: HomePostsAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileViewerBinding.bind(view)
        viewModel = ViewModelProvider(this)[ProfileViewerViewModel::class.java]

        val senderId = requireArguments().getString("senderId")

        senderId?.let {
            fetchUserData(it)
            fetchPostsData(it)
            Log.e("senderId", it)
        } ?: Log.e("senderId", "Sender ID is null.")
        adapter = HomePostsAdapter(emptyList(), object : OnProfileImageClickListener{
            override fun onProfileImageClick(senderId: String) {

            }
        })

        viewModel.postsData.observe(viewLifecycleOwner){
            adapter.setData(it)

            val layoutManager = LinearLayoutManager(requireContext())
            binding.profileRecycler.layoutManager = layoutManager
            binding.profileRecycler.adapter = adapter
        }
    }

    private fun fetchUserData(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.fetchUserData(userId)
            } catch (e: Exception) {
                Log.e("fetchUserdata", e.message.toString())
            }
        }

        viewModel.userData.observe(viewLifecycleOwner) { user ->

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

    private fun fetchPostsData(senderId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchPosts(senderId)
        }
    }

}