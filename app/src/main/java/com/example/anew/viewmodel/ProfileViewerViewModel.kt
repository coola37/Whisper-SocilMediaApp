package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Comments
import com.example.anew.model.Posts
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel class ProfileViewerViewModel @Inject constructor(
    var auth: FirebaseAuth, var db : FirebaseFirestore, application: Application ) : BaseViewModel(application){

    val userData: MutableLiveData<Users> = MutableLiveData()
    val postsData: MutableLiveData<List<Posts>> = MutableLiveData()
    val checkFollowing: MutableLiveData<Boolean> = MutableLiveData()
    val checkUserUpdate: MutableLiveData<Boolean> = MutableLiveData()
    val buttonCheck: MutableLiveData<Boolean> = MutableLiveData()
    val inMainNav: MutableLiveData<Boolean> = MutableLiveData()
    val inSearchNav: MutableLiveData<Boolean> = MutableLiveData()

    suspend fun fetchUserData(userId: String){
        val userDocRef = db.collection("users").document(userId)
        try {
            val snapshot = userDocRef.get().await()

            snapshot?.let { documentSnapshot ->
                val user = documentSnapshot.toObject(Users::class.java)
                userData.postValue(user!!)
            }
        } catch (e: Exception) {
            Log.e("ProileViewmodelFetchData",e.message.toString())
        }
    }

    suspend fun refreshUserData(userId: String){
        val userDocRef = db.collection("users").document(userId)
        try {
            val snapshot = userDocRef.get().await()

            snapshot?.let { documentSnapshot ->
                val user = documentSnapshot.toObject(Users::class.java)
                userData.postValue(user!!)
            }
        } catch (e: Exception) {
            Log.e("ProileViewmodelFetchData",e.message.toString())
        }
    }

    suspend fun fetchPosts(senderIdToFilter: String) {
        val postsCollectionRef = db.collection("posts")
        try {
            val querySnapshot = postsCollectionRef.whereEqualTo("senderID", senderIdToFilter).get().await()

            val postsList = mutableListOf<Posts>()
            for (document in querySnapshot) {
                val post = document.toObject(Posts::class.java)
                postsList.add(post)
            }

            (postsData as MutableLiveData<List<Posts>>).postValue(postsList)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching posts: ${e.message}")
        }
    }


     fun checkIfFollowing(userIdToCheck: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val currentUserUid = user.uid

            val usersCollection = db.collection("users")

            val currentUserRef = usersCollection.document(currentUserUid)

            currentUserRef.get().addOnSuccessListener { it ->
                val currentUserData = it.toObject(Users::class.java)
                val followCheck = currentUserData?.details?.listFollow?.any { it == userIdToCheck } == true
                checkFollowing.postValue(followCheck)

            }
        }
    }

    fun checkFollowButton(userIdToCheck: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val currentUserUid = user.uid

            val usersCollection = db.collection("users")

            val currentUserRef = usersCollection.document(currentUserUid)

            currentUserRef.get().addOnSuccessListener { it ->
                val currentUserData = it.toObject(Users::class.java)
                val followCheck = currentUserData?.details?.listFollow?.any { it == userIdToCheck } == true
                buttonCheck.postValue(followCheck)

            }
        }
    }


     fun followUser(senderId: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val currentUserUid = user.uid
            val usersCollection = db.collection("users")
            val followedUserRef = usersCollection.document(senderId)
            val currentUserRef = usersCollection.document(currentUserUid)

            followedUserRef.update(
                "details.listFollowers", FieldValue.arrayUnion(auth.uid),
                "details.followers", FieldValue.increment(1)
            ).addOnSuccessListener {
                Log.d("FollowUser", "User followed")

                currentUserRef.update(
                    "details.listFollow", FieldValue.arrayUnion(senderId),
                    "details.followed", FieldValue.increment(1)
                ).addOnSuccessListener {
                    Log.d("FollowUser", "User follower added")
                    checkUserUpdate.postValue(true)
                }.addOnFailureListener { e ->
                    Log.e("FollowUser", e.message.toString())
                }
            }.addOnFailureListener { e ->
                Log.e("FollowUser", e.message.toString())
            }
        }
    }

     fun unfollowUser(senderId: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val currentUserUid = user.uid
            val usersCollection = db.collection("users")
            val followedUserRef = usersCollection.document(senderId)
            val currentUserRef = usersCollection.document(currentUserUid)

            followedUserRef.update(
                "details.listFollowers", FieldValue.arrayRemove(auth.uid),
                "details.followers", FieldValue.increment(-1)
            ).addOnSuccessListener {
                Log.d("UnfollowUser", "User unfollowed")

                currentUserRef.update(
                    "details.listFollow", FieldValue.arrayRemove(senderId),
                    "details.followed", FieldValue.increment(-1)
                ).addOnSuccessListener {
                    Log.d("UnfollowUser", "User unfollower removed")
                    checkUserUpdate.postValue(true)
                }.addOnFailureListener { e ->
                    Log.e("UnfollowUser", e.message.toString())
                }
            }.addOnFailureListener { e ->
                Log.e("UnfollowUser", e.message.toString())
            }
        }
    }

}