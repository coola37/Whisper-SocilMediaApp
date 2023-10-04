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
    val checkUserupdate: MutableLiveData<Boolean> = MutableLiveData()

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
                checkUserupdate.postValue(true)
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error uploading and setting profile image: ${e.message}", e)
            }
        }
    }

    fun setCheckUpdate(case: Boolean){ checkUserupdate.postValue(case)}
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

    fun updateSenderImgInPosts(authUid: String, profileImageUrl: String, name: String) {
        launch {
            val postsCollectionRef = db.collection("posts")

            try {
                val query = postsCollectionRef.whereEqualTo("senderID", authUid)
                val querySnapshot = query.get().await()

                for (document in querySnapshot.documents) {
                    // Update the senderImg field for each matching post
                    val postRef = postsCollectionRef.document(document.id)
                    val updateDataImg = hashMapOf<String, Any>(
                        "senderImg" to profileImageUrl
                    )
                    val updateDataName = hashMapOf<String, Any>(
                        "senderName" to name
                    )
                    postRef.update(updateDataImg).await()
                    postRef.update(updateDataName).await()
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error updating senderImg in posts: ${e.message}", e)
            }
        }
    }

    fun updateImgInChatChannel(id: String, imgUrl: String){
        launch {
            val chRef = db.collection("chatChannels")
            try {
                val query = chRef.whereEqualTo("recevierId", id)
                val snapshot = query.get().await()
                for (document in snapshot.documents){
                    val channelRef = chRef.document(document.id)
                    val updateDataImg = hashMapOf<String, Any>(
                        "receiverProfileImg" to imgUrl
                    )
                    channelRef.update(updateDataImg).await()
                }
            }catch (e:Exception){
                Log.e("EditProfileViewModel", "receiver ımg in chatCh: ${e.message}")
            }
        }
    }

}

