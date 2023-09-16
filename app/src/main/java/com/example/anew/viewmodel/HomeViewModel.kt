package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Posts
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    application: Application
): BaseViewModel(application) {

    val userData: MutableLiveData<Users> = MutableLiveData()
    val postsData: MutableLiveData<List<Posts>> = MutableLiveData()
    val checkLike: MutableLiveData<Boolean> = MutableLiveData()
    val checkPostData: MutableLiveData<Boolean> = MutableLiveData()


    suspend fun refreshPostData(){
        val postsCollectionRef = db.collection("posts")
        try {
            val querySnapshot = postsCollectionRef.get().await()

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
    fun checkLike(postId: String){
        auth.currentUser?.let {

            val userId = auth.uid
            val postRef = db.collection("posts").document(postId)
            postRef.get().addOnSuccessListener {
                val postData = it.toObject(Posts::class.java)
                val checkLikes = postData?.likeUsers?.any{ it == userId  }  == true
                checkLike.postValue(checkLikes)
            }

        }
    }

    fun like(postId: String){
        val userId = auth.uid
        auth.currentUser?.let {
            val postRef = db.collection("posts").document(postId)

            postRef.update(
                "likeUsers", FieldValue.arrayUnion(userId),
                "like", FieldValue.increment(1)
            ).addOnSuccessListener {
                Log.d("Like Post ", "Post liked")
                checkPostData.postValue(true)
            }.addOnFailureListener {
                Log.e("Post Like ", it.message.toString())
            }
        }
    }

    fun disLike(postId: String){
        val userId = auth.uid
        auth.currentUser?.let {

            val postRef = db.collection("posts").document(postId)
            postRef.update(
                "likeUsers", FieldValue.arrayRemove(userId),
                "like", FieldValue.increment(-1)
            ).addOnSuccessListener {
                Log.d("post dislike", "post disliked")
                checkPostData.postValue(true)
            }.addOnFailureListener {
                Log.e("post dislike", it.message.toString())
            }

        }
    }
     suspend fun fetchPosts() {
         val postsCollectionRef = db.collection("posts")
         try {
             val querySnapshot = postsCollectionRef.get().await()

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
}