package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Messages
import com.example.anew.model.Posts
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    var auth: FirebaseAuth,
    var db: FirebaseFirestore,
    application: Application
) : BaseViewModel(application){

    val userData: MutableLiveData<Users> = MutableLiveData()
    val messages: MutableLiveData<List<Messages>> = MutableLiveData()
    val chatUsers: MutableLiveData<List<Users>> = MutableLiveData()

    suspend fun fetchUserData(userId: String){
            val userDocRef = db.collection("users").document(userId)
            try {
                val snapshot = userDocRef.get().await()

                snapshot?.let { documentSnapshot ->
                    val user = documentSnapshot.toObject(Users::class.java)
                    userData.postValue(user!!)
                }
            } catch (e: Exception) {
                Log.e("HomeViewmodelFetchData",e.message.toString())
            }
    }

    suspend fun getChatUsers(senderIdList: List<String>) {
        val chatUsersCollectionRef = db.collection("users")
        try {
            val userList = mutableListOf<Users>()

            for (senderId in senderIdList) {
                Log.e("senderID", senderId)
                val querySnapshot = chatUsersCollectionRef
                    .whereEqualTo("senderID", senderId)
                    .get()
                    .await()

                for (document in querySnapshot) {
                    val users = document.toObject(Users::class.java)
                    userList.add(users)
                }
            }

            (chatUsers as MutableLiveData<List<Users>>).postValue(userList)
        } catch (e: Exception) {
            Log.e("InboxViewModel", "Error chat users: ${e.message}")
        }
    }

}
