package com.example.anew.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.databinding.FragmentChatBinding
import com.example.anew.model.Messages
import com.example.anew.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    companion object {
        fun newInstance(senderId: String) : ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString("senderId", senderId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatViewModel
    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    @Inject
    internal lateinit var glide: RequestManager
    private lateinit var receiverId : String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        receiverId = requireArguments().getString("senderId").toString()

        CoroutineScope(Dispatchers.IO).async {
            auth.currentUser?.let {
                viewModel.fetchSenderUser(auth.uid!!)
                viewModel.fetchReceiverUser(receiverId)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            getReceiverUserData()
        }

        binding.buttonSendMsg.setOnClickListener {
            binding.editTextTextMsg.text.clear()
            sendMessage()
        }
    }

    private fun sendMessage(){
        val msgText = binding.editTextTextMsg.text.toString()
        val date = SimpleDateFormat("dd/M/yyyy hh:mm").format(Date())
        val msgId = UUID.randomUUID().toString()
        val msg = Messages(msgId, auth.uid, receiverId, msgText, date)
        CoroutineScope(Dispatchers.Main).launch { viewModel.saveMsgToDb(msg) }
    }
    private fun getReceiverUserData(){
        viewModel.receiverUser.observe(viewLifecycleOwner){

            binding.receiverUsername.text = it.username
            binding.receiverName.text = it.details?.name
            glide.load(it.details?.profileImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.receiverProfileImg)

        }
    }
}