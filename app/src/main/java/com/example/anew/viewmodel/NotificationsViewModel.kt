package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Comments
import com.example.anew.model.Notifications
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    var auth: FirebaseAuth,
    var db: FirebaseFirestore,
    application: Application
): BaseViewModel(application){

    val notificationsData: MutableLiveData<List<Notifications>> = MutableLiveData()

    suspend fun fetchNotifications(userId: String){
        val notificationRef = db.collection("notifications").whereEqualTo("targetUser", userId)
        try {
            val snapshot = notificationRef.get().await()
            val list = mutableListOf<Notifications>()
            for(document in snapshot){
                val notification = document.toObject(Notifications::class.java)
                list.add(notification)
            }
            (notificationsData as MutableLiveData<List<Notifications>>).postValue(list)
        }catch (e: java.lang.Exception){
            Log.e("notificationList", e.message.toString())
        }
    }
}