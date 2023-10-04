package com.example.anew.view

import android.os.Bundle
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
import com.example.anew.R
import com.example.anew.adapter.ChatChannelAdapter
import com.example.anew.adapter.OnClickListenerCatchData
import com.example.anew.adapter.UsersAdapter
import com.example.anew.databinding.FragmentInboxBinding
import com.example.anew.viewmodel.InboxViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects
import javax.inject.Inject

@AndroidEntryPoint
class InboxFragment : Fragment(R.layout.fragment_inbox) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    @Inject
    internal lateinit var glide: RequestManager
    private lateinit var binding: FragmentInboxBinding
    private lateinit var viewModel: InboxViewModel
    private lateinit var adapter: ChatChannelAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInboxBinding.bind(view)
        viewModel = ViewModelProvider(this)[InboxViewModel::class.java]

       auth.currentUser?.let {
           CoroutineScope(Dispatchers.IO).launch {
               viewModel.fetchUserData(auth.uid!!)
               viewModel.fetchChatChannels(auth.uid!!)
           }
       }

        viewModel.chatChannels.observe(viewLifecycleOwner){
            adapter = ChatChannelAdapter(it, object : OnClickListenerCatchData{
                override fun onProfileImageClick(senderId: String) {
                    findNavController().navigate(R.id.action_inboxFragment_to_chatFragment,
                    bundleOf("senderId" to senderId))
                }
            })
            val manager = LinearLayoutManager(requireContext())
            binding.msgRecycler.layoutManager = manager
            binding.msgRecycler.adapter = adapter
        }
    }
}