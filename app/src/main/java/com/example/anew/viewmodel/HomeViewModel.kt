package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    application: Application
): BaseViewModel(application) {

    val userData: MutableLiveData<Users> = MutableLiveData()

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