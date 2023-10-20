package com.example.anew.view

import android.content.Intent
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
import com.example.anew.adapter.CommentsAdapter
import com.example.anew.adapter.OnClickListenerCatchData
import com.example.anew.databinding.FragmentPostViewerBinding
import com.example.anew.model.Comments
import com.example.anew.viewmodel.PostViewerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
 import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PostViewerFragment : Fragment(R.layout.fragment_post_viewer) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    @Inject
    internal lateinit var glide: RequestManager
    private lateinit var binding: FragmentPostViewerBinding
    private lateinit var viewModel: PostViewerViewModel
    private lateinit var commentsAdapter: CommentsAdapter

    private var postID: String =""
    private var username: String =""
    private var senderProfileImg: String =""
    private var senderId: String =""

    companion object {
        fun newInstance(postID: String) : PostViewerFragment {
            val fragment = PostViewerFragment()
            val args = Bundle()
            args.putString("postID", postID)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPostViewerBinding.bind(view)
        viewModel = ViewModelProvider(this)[PostViewerViewModel::class.java]
        postID = requireArguments().getString("postID").toString()

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.fetchUserData(auth.uid!!)
            viewModel.fetchPostData(postID)
            viewModel.fetchCommentsData(postID)

        }
        CoroutineScope(Dispatchers.Main).launch {
            getPostData()
            getUserData()
        }

        commentsAdapter = CommentsAdapter(emptyList(), object : OnClickListenerCatchData{
            override fun onProfileImageClick(senderId: String) {
                if(auth.uid != senderId){
                    findNavController().navigate(R.id.action_postViewerFragment_to_profileViewerFragment,
                        bundleOf("senderId" to senderId))
                }else{
                    val intent = Intent(requireContext(), ProfileActivity::class.java)
                    startActivity(intent)
                }
            }
        },
        object : OnClickListenerCatchData{
            override fun onProfileImageClick(senderId: String) {
               likeAndDislikeForComments(senderId)
            }

        })

        viewModel.commentsData.observe(viewLifecycleOwner){
            commentsAdapter.setData(it)
            val layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerComments.layoutManager = layoutManager
            binding.recyclerComments.adapter = commentsAdapter
        }

        setupButtonClick()

    }

    private fun likeAndDislike(postID: String) {
        CoroutineScope(Dispatchers.IO).launch { viewModel.checkLike(postID) }
        viewModel.checkLike.observe(viewLifecycleOwner){
            if(it){
                CoroutineScope(Dispatchers.Main).launch{
                    viewModel.disLike(postID)
                    viewModel.refreshPostData(postID) }
            }else{
                CoroutineScope(Dispatchers.Main).launch{
                    viewModel.like(postID)
                    viewModel.refreshPostData(postID) }
            }
        }
    }

    private fun likeAndDislikeForComments(commentId: String){
        CoroutineScope(Dispatchers.IO).launch{viewModel.checkLikeForComments(commentId)}
        viewModel.checkLikeForComments.observe(viewLifecycleOwner){
            if(it){
                viewModel.disLikeForComments(commentId)
                CoroutineScope(Dispatchers.Main).launch{viewModel.refreshCommentsData(postID)}
            }else{
                viewModel.likeForComments(commentId)
                CoroutineScope(Dispatchers.Main).launch { viewModel.refreshCommentsData(postID) }
            }
        }
    }



    private fun getPostData(){
        viewModel.postsData.observe(viewLifecycleOwner){
            senderId = it.senderID!!
            binding.textViewName.text = it.senderName
            binding.textViewUsername.text = it.senderUsername
            binding.textViewPostText.text = it.text
            binding.textViewDate.text = it.date
            binding.textViewLikeCount.text = it.like.toString()
            val postImg = it.postImg.toString()
            if (postImg == " "){
                binding.imageViewPostImg.visibility = View.GONE

            }else{
                glide.load(it.postImg)
                    .placeholder(R.mipmap.ic_none_img)
                    .error(R.mipmap.ic_none_img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.imageViewPostImg)
            }

            Log.e("postImg", postImg)

            glide.load(it.senderImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_none_img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imagePostProfil)
        }
    }

    private fun getUserData(){
        viewModel.userData.observe(viewLifecycleOwner){
            glide.load(it.details?.profileImg)
                .placeholder(R.mipmap.ic_none_img)
                .error(R.mipmap.ic_none_img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imageProfile)
            username = it.username!!
            senderProfileImg = it.details?.profileImg!!
        }
    }

    private fun sendComment(comments: Comments){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                viewModel.saveCommentToDb(comments)
            }catch (e: Exception){
                Log.e("sendComment", e.message.toString())
            }
        }
    }

    private fun setupButtonClick(){
        binding.buttonAddComment.setOnClickListener {
            val commentId = UUID.randomUUID().toString()
            val commentText = binding.editTextComments.text.toString()
            val comments = Comments(commentId, auth.uid, postID, username, senderProfileImg, 0, emptyList(), commentText)
            sendComment(comments)
            binding.editTextComments.text.clear()
        }
        binding.imageViewLike.setOnClickListener {
            likeAndDislike(postID)
        }
    }
}