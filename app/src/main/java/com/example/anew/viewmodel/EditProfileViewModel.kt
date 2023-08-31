package com.example.anew.viewmodel

import androidx.lifecycle.ViewModel
import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    application: Application
) : BaseViewModel(application)  {
    val userData: MutableLiveData<Users> = MutableLiveData()

     suspend fun uploadProfileImageAndGetUrl(userID: String, imgUri: Uri): String {
        return withContext(Dispatchers.IO) {
            return@withContext suspendCancellableCoroutine<String> { continuation ->
                val storageRef = storage.reference.child("profile_images").child("$userID.jpg")
                val uploadTask = storageRef.putFile(imgUri)

                uploadTask.addOnSuccessListener { uploadTaskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        continuation.resume(downloadUrl.toString())
                    }.addOnFailureListener { downloadUrlError ->
                        continuation.resumeWithException(downloadUrlError)
                    }
                }.addOnFailureListener { uploadTaskError ->
                    continuation.resumeWithException(uploadTaskError)
                }

                continuation.invokeOnCancellation {
                    uploadTask.cancel()
                }
            }
        }
    }

     fun updateProfileData(userId: String, name: String, bio: String, profileImageUrl: String) {
        launch {
            val userDocRef = db.collection("users").document(userId)

            val updateData = hashMapOf<String, Any>(
                "details.name" to name,
                "details.bio" to bio,
                "details.profileImg" to profileImageUrl
            )

            try {
                userDocRef.update(updateData as Map<String, Any>).await()
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error uploading and setting profile image: ${e.message}", e)
            }
        }
    }

     fun uploadAndSetProfileImage(userId: String, imgUri: Uri, name: String, bio: String) {
        launch {
            val imageUrl = uploadProfileImageAndGetUrl(userId, imgUri)
            updateProfileData(userId, name, bio, imageUrl)
        }
    }

     fun fetchUserData(userId: String){
       launch {
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
}

