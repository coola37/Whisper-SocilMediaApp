package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Messages
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

    suspend fun saveMsgToDb(msg: Messages){
        val senderChannel = msg.recevierId + msg.senderId
        val receiverChannel = msg.senderId + msg.recevierId

        try {
            db.collection("messages").document(senderChannel ?: "")
                .collection("chats").document(msg.messageId ?: "").set(msg).await()
            db.collection("messages").document(receiverChannel ?: "").
                    collection("chats").document(msg.messageId ?: "").set(msg).await()


        }catch (e: java.lang.Exception){
            Log.e("Msg save to db error", e.message.toString())
        }
    }


}
