package com.example.anew.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    var auth: FirebaseAuth,
    var db: FirebaseFirestore,
    application: Application
) : BaseViewModel(application){

    val senderUser: MutableLiveData<Users> = MutableLiveData()
    val receiverUser: MutableLiveData<Users> = MutableLiveData()

}