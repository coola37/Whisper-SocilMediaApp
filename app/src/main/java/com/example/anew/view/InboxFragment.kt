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
    private var mutuallyUsers: List<String> = emptyList()
    private lateinit var adapter: UsersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInboxBinding.bind(view)
        viewModel = ViewModelProvider(this)[InboxViewModel::class.java]

        auth.currentUser?.let {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.fetchUserData(auth.uid!!)
            }

            viewModel.userData.observe(viewLifecycleOwner){
                mutuallyUsers = it.details?.listFollow!!
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.getChatUsers(mutuallyUsers)
                }
            }

            viewModel.chatUsers.observe(viewLifecycleOwner){
                adapter = UsersAdapter(it, object : OnClickListenerCatchData{
                    override fun onProfileImageClick(senderId: String) {
                        findNavController().navigate(R.id.action_inboxFragment_to_chatFragment, bundleOf
                            ("receiverId" to senderId))
                    }
                })

                val layoutManager = LinearLayoutManager(requireContext())
                binding.msgRecycler.layoutManager = layoutManager
                binding.msgRecycler.adapter = adapter
            }

        }


    }
}