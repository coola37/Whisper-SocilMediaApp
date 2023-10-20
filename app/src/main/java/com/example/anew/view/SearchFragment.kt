package com.example.anew.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.adapter.OnClickListenerCatchData
import com.example.anew.adapter.UsersAdapter
import com.example.anew.databinding.FragmentSearchBinding
import com.example.anew.viewmodel.SearchViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding : FragmentSearchBinding
    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    @Inject
    internal lateinit var glide: RequestManager
    private lateinit var adapter: UsersAdapter
    private lateinit var viewModel : SearchViewModel
    private var imgUrl : String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        CoroutineScope(Dispatchers.IO).launch{
            viewModel.fetchUsers()
            viewModel.fetchUserData(auth.uid!!)
        }

        CoroutineScope(Dispatchers.Main).launch {
            binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.search(newText ?: "")
                    return true
                }

            })
            adapter = UsersAdapter(emptyList(), object : OnClickListenerCatchData{
                override fun onProfileImageClick(senderId: String) {
                    val fragment = ProfileViewerFragment.newInstance(senderId)
                    Log.e("senderId SearchFragmentToViewerFragment", senderId)
                    findNavController().navigate(R.id.action_searchFragment_to_profileViewerFragment2, bundleOf("senderId" to senderId))
                }

            })
            getUsersData()
        }


    }

    private fun getUsersData(){
        viewLifecycleOwner.lifecycleScope.launch {
         viewModel.usersData.observe(viewLifecycleOwner){
             adapter.setData(it)

             val layoutManager = LinearLayoutManager(requireContext())
             binding.searchRecycler.layoutManager = layoutManager
             binding.searchRecycler.adapter = adapter
         }
            viewModel.currencyUser.observe(viewLifecycleOwner){
                glide.load(it.details?.profileImg)
                    .placeholder(R.mipmap.ic_none_img)
                    .error(R.mipmap.ic_none_img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.circleImage)
            }
        }

    }

}