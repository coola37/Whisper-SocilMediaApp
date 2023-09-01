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

@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    application: Application
): BaseViewModel(application) {

    val userData: MutableLiveData<Users> = MutableLiveData()
    val postsData: MutableLiveData<List<Posts>> = MutableLiveData()


     fun fetchPosts() {
         launch {
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

    }
     fun fetchUserData(userId: String){
        launch {

        }
    }
}