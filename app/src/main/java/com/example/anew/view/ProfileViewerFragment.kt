package com.example.anew.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.anew.R
import com.example.anew.adapter.HomePostsAdapter
import com.example.anew.adapter.OnProfileImageClickListener
import com.example.anew.databinding.FragmentProfileViewerBinding
import com.example.anew.model.Users
import com.example.anew.viewmodel.ProfileViewerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldValue.arrayUnion
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
    private var currentUserModel = Users()
    private var followedUser = Users()
    private lateinit var senderId : String



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileViewerBinding.bind(view)
        viewModel = ViewModelProvider(this)[ProfileViewerViewModel::class.java]

        senderId = requireArguments().getString("senderId").toString()

        CoroutineScope(Dispatchers.IO).async {
            viewModel.fetchUserData(senderId)
            viewModel.fetchPosts(senderId)
        }


        CoroutineScope(Dispatchers.Main).async{

            getUserData()
            getPostData()
            checkIfFollowing(senderId)
        }

        /*  senderId?.let {

            Log.e("senderId", it)
        } ?: Log.e("senderId", "Sender ID is null.")
*/

        adapter = HomePostsAdapter(emptyList(), object : OnProfileImageClickListener{
            override fun onProfileImageClick(senderId: String) {

            }
        })
        binding.buttonFollow.setOnClickListener {
           viewLifecycleOwner.lifecycleScope.launch {
               viewModel.checkIfFollowing(senderId){
                   if(it){
                       getUserData()
                       viewLifecycleOwner.lifecycleScope.launch { unfollowUser(senderId) }
                   }
                   else{
                       getUserData()
                       viewLifecycleOwner.lifecycleScope.launch { followUser(senderId) }
                   }
               }
           }
        }

    }


    private fun getPostData(){
        viewModel.postsData.observe(viewLifecycleOwner){
            adapter.setData(it)
            adapter.notifyDataSetChanged()

            val layoutManager = LinearLayoutManager(requireContext())
            binding.profileRecycler.layoutManager = layoutManager
            binding.profileRecycler.adapter = adapter
        }
    }


    private  fun checkIfFollowing(senderId: String){
        viewModel.checkIfFollowing(senderId!!){
            if(it){
                binding.buttonFollow.setBackgroundResource(R.drawable.active_bt_bg)
                binding.buttonFollow.text = "Unfollow"
                binding.buttonFollow.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            else{
                binding.buttonFollow.setBackgroundResource(R.drawable.bt_login_bg)
                binding.buttonFollow.text = "Follow"
                binding.buttonFollow.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }


    private  fun getUserData() {
        CoroutineScope(Dispatchers.Main).async {
            viewModel.userData.observe(viewLifecycleOwner) { user ->
                followedUser = user
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
    }


    private  fun followUser(userIdToFollow: String) {
        viewLifecycleOwner.lifecycleScope.launch{
            val currentUser = auth.currentUser
            currentUser?.let { user ->
                val currentUserUid = user.uid

                val usersCollection = db.collection("users")

                val followedUserRef = usersCollection.document(userIdToFollow)
                val currentUserRef = usersCollection.document(currentUserUid)



                usersCollection.document(currentUserUid)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val user = documentSnapshot.toObject(Users::class.java)
                            currentUserModel = user!!
                        } else {
                            // Belirtilen kullanıcıya ait veri bulunamadı
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FetchUserData", "Hata: ${e.message}", e)
                    }

                followedUserRef.update(
                    "details.listFollowers", FieldValue.arrayUnion(currentUserModel.userId),
                    "details.followers", FieldValue.increment(1)
                ).addOnSuccessListener {
                    Log.d("FollowUser", "User followed")

                    currentUserRef.update(
                        "details.listFollow", FieldValue.arrayUnion(followedUser.userId),
                        "details.followed", FieldValue.increment(1)
                    ).addOnSuccessListener {
                        Log.d("FollowUser", "User follower added")
                    }.addOnFailureListener { e ->
                        Log.e("FollowUser", e.message.toString())
                    }
                }.addOnFailureListener { e ->
                    Log.e("FollowUser", e.message.toString() )
                }
            }
        }
    }
    private  fun unfollowUser(userIdToUnfollow: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentUser = auth.currentUser
            currentUser?.let { user ->
                val currentUserUid = user.uid

                val usersCollection = db.collection("users")

                val followedUserRef = usersCollection.document(userIdToUnfollow)
                val currentUserRef = usersCollection.document(currentUserUid)

                usersCollection.document(currentUserUid)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val user = documentSnapshot.toObject(Users::class.java)
                            currentUserModel = user!!
                        } else {
                            // Belirtilen kullanıcıya ait veri bulunamadı
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FetchUserData", "Hata: ${e.message}", e)
                    }
                followedUserRef.update(
                    "details.listFollowers", FieldValue.arrayRemove(currentUserModel.userId),
                    "details.followers", FieldValue.increment(-1)
                ).addOnSuccessListener {
                    Log.d("UnfollowUser", "User unfollowed")

                    currentUserRef.update(
                        "details.listFollow", FieldValue.arrayRemove(userIdToUnfollow),
                        "details.followed", FieldValue.increment(-1)
                    ).addOnSuccessListener {
                        Log.d("UnfollowUser", "User unfollower removed")
                    }.addOnFailureListener { e ->
                        Log.e("UnfollowUser", e.message.toString())
                    }
                }.addOnFailureListener { e ->
                    Log.e("UnfollowUser", e.message.toString() )
                }
            }
        }
    }
}