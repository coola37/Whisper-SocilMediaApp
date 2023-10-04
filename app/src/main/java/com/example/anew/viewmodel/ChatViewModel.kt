package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.ChatChannel
import com.example.anew.model.Messages
import com.example.anew.model.Users
import com.google.android.play.integrity.internal.c
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    var auth: FirebaseAuth,
    var db: FirebaseFirestore,
    application: Application
) : BaseViewModel(application){

    val senderUser: MutableLiveData<Users> = MutableLiveData()
    val receiverUser: MutableLiveData<Users> = MutableLiveData()
    val msgData: MutableLiveData<ChatChannel> = MutableLiveData()
    val checkGetMessages: MutableLiveData<Boolean> = MutableLiveData()

    suspend fun fetchSenderUser(userId: String){
        val userDocRef = db.collection("users").document(userId)
        try {
            val snapshot = userDocRef.get().await()

            snapshot?.let { documentSnapshot ->
                val user = documentSnapshot.toObject(Users::class.java)
                senderUser.postValue(user!!)
            }
        } catch (e: Exception) {
            Log.e("ChatViewmodelSenderFetchData",e.message.toString())
        }
    }

    suspend fun fetchReceiverUser(userId: String){
        val userDocRef = db.collection("users").document(userId)
        try {
            val snapshot = userDocRef.get().await()

            snapshot?.let { documentSnapshot ->
                val user = documentSnapshot.toObject(Users::class.java)
                receiverUser.postValue(user!!)
            }
        } catch (e: Exception) {
            Log.e("ChatViewmodelReceiverFetchData",e.message.toString())
        }
    }

    suspend fun fetchMessages(senderId: String, receiverId: String){
        val ch = senderId + receiverId
        val msgCollectionRef = db.collection("chatChannels").document(ch)
        try {
            val querySnapshot = msgCollectionRef.get().await()
            querySnapshot?.let {documentSnapshot ->
                val chatCh = documentSnapshot.toObject(ChatChannel::class.java)
                msgData.postValue(chatCh!!)
            }

        }catch (e: java.lang.Exception){
            Log.e("fetchMessages", e.message.toString())
        }
    }

    suspend fun RefreshMessagesData(senderId: String, receiverId: String){
        val ch = senderId + receiverId
        val msgCollectionRef = db.collection("chatChannels").document(ch)
        try {
            val querySnapshot = msgCollectionRef.get().await()
            querySnapshot?.let {documentSnapshot ->
                val chatCh = documentSnapshot.toObject(ChatChannel::class.java)
                msgData.postValue(chatCh!!)
            }

        }catch (e: java.lang.Exception){
            Log.e("fetchMessages", e.message.toString())
        }
    }


     fun saveMsgToDb(msg: Messages) {
        val senderChannel = msg.recevierId + msg.senderId
        val receiverChannel = msg.senderId + msg.recevierId

        try {
            val msgRefSender = db.collection("chatChannels").document(senderChannel)
            val msgRefReceiver = db.collection("chatChannels").document(receiverChannel)

            val updateData = mapOf(
                "messages" to FieldValue.arrayUnion(msg),
                "lastMessage" to msg.msgText
            )

            msgRefSender.update(updateData).addOnSuccessListener {
                Log.d("sendMsg", "ok")
            }.addOnFailureListener {
                Log.e("sendMsgError", it.message.toString())
            }

            msgRefReceiver.update(updateData).addOnSuccessListener {
                Log.d("sendMsg", "ok")
            }.addOnFailureListener {
                Log.e("sendMsgError", it.message.toString())
            }
            checkGetMessages.postValue(true)
        } catch (e: java.lang.Exception) {
            Log.e("Msg save to db error", e.message.toString())
        }
    }


    suspend fun firstMsg(chatSenderCh: ChatChannel, chatReceiverCh: ChatChannel, msg: Messages){
        val senderCh = chatSenderCh.senderId + chatSenderCh.recevierId
        val receiverCh = chatReceiverCh.senderId + chatReceiverCh.recevierId

        try{
            db.collection("chatChannels").document(senderCh).set(chatSenderCh).await()
            db.collection("chatChannels").document(receiverCh).set(chatReceiverCh).await()
        }catch (e: java.lang.Exception){
            Log.e("InboxFragmentFirstMsg", e.message.toString())
        }

        saveMsgToDb(msg)
        checkGetMessages.postValue(true)
    }
}