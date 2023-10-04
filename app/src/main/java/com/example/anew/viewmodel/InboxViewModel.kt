package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.ChatChannel
import com.example.anew.model.Messages
import com.example.anew.model.Posts
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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
    val chatChannels: MutableLiveData<List<ChatChannel>> = MutableLiveData()
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

   suspend fun fetchChatChannels(userId: String){
       val chatChannelDocRef = db.collection("chatChannels")
       try {
           val querySnapshot = chatChannelDocRef.whereEqualTo("senderId", userId).get().await()

           val chatChannelList = mutableListOf<ChatChannel>()
           for(document in querySnapshot){
               val channel = document.toObject(ChatChannel::class.java)
               chatChannelList.add(channel)
           }
           (chatChannels as MutableLiveData<List<ChatChannel>>).postValue(chatChannelList)
       }catch (e: Exception){
           Log.e("InboxFragmentFetchChats", e.message.toString())
       }
   }

}
