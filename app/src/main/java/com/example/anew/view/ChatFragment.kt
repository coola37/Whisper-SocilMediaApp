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
import com.example.anew.adapter.ChatAdapter
import com.example.anew.databinding.FragmentChatBinding
import com.example.anew.model.ChatChannel
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
    private var senderUsername: String = ""
    private lateinit var adapter : ChatAdapter
    private var senderProfileImg: String = ""
    private var receiverUsername: String = ""
    private var receiverProfileImg: String= ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        receiverId = requireArguments().getString("senderId").toString()
        adapter = ChatAdapter(requireContext(), emptyList())

        CoroutineScope(Dispatchers.IO).async {
            auth.currentUser?.let {
                viewModel.fetchSenderUser(auth.uid!!)
                viewModel.fetchReceiverUser(receiverId)
                viewModel.fetchMessages(auth.uid.toString(), receiverId)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            getReceiverUserData()
            getSenderUserData()

            viewModel.msgData.observe(viewLifecycleOwner){
                adapter.setData(it.messages!!)

                val layoutManager = LinearLayoutManager(requireContext())
                binding.chatRecycler.layoutManager = layoutManager
                binding.chatRecycler.adapter = adapter
            }

        }

        binding.buttonSendMsg.setOnClickListener {
           CoroutineScope(Dispatchers.Main).launch {
               sendMessage()
               binding.editTextTextMsg.text.clear()

               viewModel.checkGetMessages.observe(viewLifecycleOwner){
                   if (it){
                       CoroutineScope(Dispatchers.Main).launch { viewModel.RefreshMessagesData(auth.uid.toString(), receiverId) }
                   }else{
                       Log.d("Chat Data", "chat data is currency")
                   }
               }
           }
        }
        setupButtons()
    }

    private fun sendMessage(){
        val msgText = binding.editTextTextMsg.text.toString()
        val date = SimpleDateFormat("dd/M/yyyy hh:mm").format(Date())
        val msgId = UUID.randomUUID().toString()
        val list: List<Messages>? = emptyList()
        val msg = Messages(msgId, auth.uid, receiverId, msgText, date, senderUsername, senderProfileImg)
        val chSender = ChatChannel(auth.uid, receiverId, receiverProfileImg,receiverUsername, list, msgText )
        val chReceiver = ChatChannel(receiverId,auth.uid, senderProfileImg, senderUsername, list, msgText)

        val channel = auth.uid + receiverId
        val docRef = db.collection("chatChannels").document(channel)

        docRef.get().addOnSuccessListener {
                if (it.exists()){
                    CoroutineScope(Dispatchers.Main).async { viewModel.saveMsgToDb(msg) }
                }else{
                    CoroutineScope(Dispatchers.Main).async { viewModel.firstMsg(chSender, chReceiver, msg) }
                }
            }.addOnFailureListener {
                Log.e("InboxFragmentSendMsg", it.message.toString())
            }
    }
    private fun getReceiverUserData(){
        viewModel.receiverUser.observe(viewLifecycleOwner){

            binding.receiverUsername.text = it.username
            binding.receiverName.text = it.details?.name
            receiverUsername = it.username!!
            receiverProfileImg = it.details?.profileImg!!

            glide.load(it.details?.profileImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.receiverProfileImg)

        }
    }

    private fun getSenderUserData(){
        viewModel.senderUser.observe(viewLifecycleOwner){
            senderUsername = it.username!!
            senderProfileImg = it.details?.profileImg!!
        }
    }

    private fun setupButtons(){
        binding.receiverProfileImg.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_profileViewerFragment,
            bundleOf("senderId" to receiverId)
            )
        }
        binding.backImg.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_inboxFragment)
        }
    }

}