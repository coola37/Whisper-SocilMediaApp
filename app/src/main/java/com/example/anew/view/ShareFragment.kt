package com.example.anew.view

import android.os.Bundle
import android.util.Log
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
import com.example.anew.databinding.FragmentSearchBinding
import com.example.anew.databinding.FragmentShareBinding
import com.example.anew.viewmodel.ShareViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.InputEventMask
import javax.inject.Inject

@AndroidEntryPoint
class ShareFragment : Fragment(R.layout.fragment_share) {

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var db: FirebaseFirestore
    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentShareBinding
    private lateinit var viewModel: ShareViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShareBinding.bind(view)
        viewModel = ViewModelProvider(this)[ShareViewModel::class.java]
        binding.imageViewClose.setOnClickListener {
            findNavController().navigate(R.id.action_shareFragment_to_homeFragment)
        }

        fetchUserData(auth.uid!!)

    }

    private fun fetchUserData(userId:String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.fetchUserData(userId)
            }catch (e: Exception){
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
}