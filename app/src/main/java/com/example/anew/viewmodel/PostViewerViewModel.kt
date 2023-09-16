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
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PostViewerViewModel @Inject constructor(
    application: Application,
    val auth: FirebaseAuth,
    val db: FirebaseFirestore
) : BaseViewModel(application){

    val userData: MutableLiveData<Users> = MutableLiveData()
    val postsData: MutableLiveData<Posts> = MutableLiveData()
    val checkUpdate: MutableLiveData<Boolean> = MutableLiveData()
    val checkLike: MutableLiveData<Boolean> = MutableLiveData()
    val commentsData: MutableLiveData<List<Comments>> = MutableLiveData()
    val checkLikeForComments: MutableLiveData<Boolean> = MutableLiveData()
    val checkUpdateComments: MutableLiveData<Boolean> = MutableLiveData()

    suspend fun refreshPostData(postId: String){
        val postsCollectionRef = db.collection("posts").document(postId)
        try {
            val snapshot = postsCollectionRef.get().await()
            snapshot?.let {
                val post = it.toObject(Posts::class.java)
                postsData.postValue(post!!)
                checkUpdate.postValue(false)
            }
        }catch (e: Exception){
            Log.e("PostViewerViewModel_refreshPostData", e.message.toString())
        }

    }

    suspend fun refreshCommentsData(postId: String){
        val commentsCollectionRef = db.collection("comments")
        try {
            val querySnapshot = commentsCollectionRef.whereEqualTo("postID", postId).get().await()


            val commentsList = mutableListOf<Comments>()
            for (document in querySnapshot){
                val comment = document.toObject(Comments::class.java)
                commentsList.add(comment)
            }
            (commentsData as MutableLiveData<List<Comments>>).postValue(commentsList)
        }catch (e: java.lang.Exception){
            Log.e("PostViewerVM fetchComments", e.message.toString())
        }
    }
    suspend fun fetchPostData(postId: String){
        val postsCollectionRef = db.collection("posts").document(postId)
        try {
            val snapshot = postsCollectionRef.get().await()
            snapshot?.let {
                val post = it.toObject(Posts::class.java)
                postsData.postValue(post!!)
            }
        }catch (e: Exception){
            Log.e("PostViewerViewModel_fetchPostData", e.message.toString())
        }
    }


    suspend fun fetchCommentsData(postId: String){
        val commentsCollectionRef = db.collection("comments")
        try {
            val querySnapshot = commentsCollectionRef.whereEqualTo("postID", postId).get().await()


            val commentsList = mutableListOf<Comments>()
            for (document in querySnapshot){
                val comment = document.toObject(Comments::class.java)
                commentsList.add(comment)
            }
            (commentsData as MutableLiveData<List<Comments>>).postValue(commentsList)
        }catch (e: java.lang.Exception){
            Log.e("PostViewerVM fetchComments", e.message.toString())
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

    fun checkLikeForComments(commentId: String){
        auth.currentUser?.let {

            val userId = auth.uid
            val postRef = db.collection("comments").document(commentId)
            postRef.get().addOnSuccessListener {
                val commentData = it.toObject(Comments::class.java)
                val checkLikes = commentData?.likeUsers?.any{ it == userId  }  == true
                checkLikeForComments.postValue(checkLikes)
            }

        }
    }

    fun likeForComments(commentId: String){
        val userId = auth.uid
        auth.currentUser?.let {
            val commentRef = db.collection("comments").document(commentId)

            commentRef.update(
                "likeUsers", FieldValue.arrayUnion(userId),
                "likeCounts", FieldValue.increment(1)
            ).addOnSuccessListener {
                Log.d("Comment Post ", "Comment liked")
                checkUpdateComments.postValue(true)
            }.addOnFailureListener {
                Log.e("Comment Like ", it.message.toString())
            }
        }
    }

    fun disLikeForComments(commentId: String){
        val userId = auth.uid
        auth.currentUser?.let {

            val commentRef = db.collection("comments").document(commentId)
            commentRef.update(
                "likeUsers", FieldValue.arrayRemove(userId),
                "likeCounts", FieldValue.increment(-1)
            ).addOnSuccessListener {
                Log.d("comment dislike", "post disliked")
                checkUpdateComments.postValue(true)
            }.addOnFailureListener {
                Log.e("comment dislike", it.message.toString())
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
                checkUpdate.postValue(true)
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
                checkUpdate.postValue(true)
            }.addOnFailureListener {
                Log.e("post dislike", it.message.toString())
            }
        }
    }

    suspend fun saveCommentToDb(comments: Comments){
        try {
            db.collection("comments").document(comments.commentsID ?: "").set(comments).await()
        } catch (e: java.lang.Exception) {
            Log.e("SaveCommentsDb", e.message.toString())
        }
    }


}