package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Posts
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
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
}