package com.example.anew.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.anew.R
import com.example.anew.adapter.NotificationAdapter
import com.example.anew.adapter.OnClickListenerCatchData
import com.example.anew.databinding.FragmentNotificationsBinding
import com.example.anew.viewmodel.NotificationsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    @Inject
    internal lateinit var glide: RequestManager

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var viewModel: NotificationsViewModel
    private lateinit var adapter: NotificationAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationsBinding.bind(view)
        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        setupBottomNavigationView()
        adapter = NotificationAdapter(emptyList(), object : OnClickListenerCatchData{
            override fun onProfileImageClick(senderId: String) {
                findNavController().navigate(R.id.action_notificationsFragment_to_postViewerFragment,
                bundleOf("postID" to senderId)
                )
            }

        })
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.fetchNotifications(auth.uid!!)
        }

        viewModel.notificationsData.observe(viewLifecycleOwner){
            adapter.setData(it)
            val layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewNotification.layoutManager=layoutManager
            binding.recyclerViewNotification.adapter = adapter
        }
    }

    private fun setupBottomNavigationView(){
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {

            when(it.itemId){

                R.id.ic_action_search -> {
                    Log.e("search","click")
                    findNavController().navigate(R.id.action_notificationsFragment_to_searchFragment)
                    false
                }
                R.id.ic_action_home -> {
                    Log.e("notification","click")
                    findNavController().navigate(R.id.action_notificationsFragment_to_homeFragment)
                    false
                }
                R.id.ic_action_inbox -> {
                    Log.e("inbox","click")
                    findNavController().navigate(R.id.action_notificationsFragment_to_inboxFragment)
                    false
                }

                else -> false
            }
        }
        val menuItem = binding.bottomNavigationView.menu.getItem(0)
        menuItem.isChecked = true
    }
}