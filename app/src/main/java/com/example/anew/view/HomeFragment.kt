package com.example.anew.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.databinding.FragmentHomeBinding
import com.example.anew.viewmodel.EditProfileViewModel
import com.example.anew.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.InputEventMask
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var db: FirebaseFirestore
    @Inject
    lateinit var glide: RequestManager
    private lateinit var viewModel: HomeViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setupBottomNavigationView()
        setupButtonClick()
        fetchUserData(auth.uid!!)

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
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.fetchUserData(userId)
            } catch (e: Exception) {
                Log.e("homeFragmentFetchUserdata", e.message.toString())
            }
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
