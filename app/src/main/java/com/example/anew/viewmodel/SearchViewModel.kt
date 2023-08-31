package com.example.anew.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    val auth : FirebaseAuth,
    val db: FirebaseFirestore
) : BaseViewModel(application){

    val usersData : MutableLiveData<List<Users>> = MutableLiveData()
    val currencyUser : MutableLiveData<Users> = MutableLiveData()

    private val usersNameMap : MutableMap<String, Users> = LinkedHashMap()

    fun search(query: String){
        if(query.isBlank()){
            showUsers(ArrayList(usersNameMap.values))
        }else{
            launch {
                val matchedUsers = usersNameMap.keys.filter { it.contains(query, ignoreCase = true) }
                val matchedUserList: MutableList<Users> = ArrayList(matchedUsers.size)
                matchedUsers.forEach {
                    matchedUserList.add(usersNameMap[it]!!)
                }
                withContext(Dispatchers.Main){
                    showUsers(matchedUserList)
                }
            }
        }
    }

    fun showUsers(usersList : List<Users>){
        usersData.value = usersList
    }
    fun fetchUsers() {
        launch {
            val usersCollectionRef = db.collection("users")
            try {
                val querySnapshot = usersCollectionRef.get().await()

                val usersList = mutableListOf<Users>()
                for (document in querySnapshot){
                    val user = document.toObject(Users::class.java)
                    usersList.add(user)
                }
                (usersData as MutableLiveData<List<Users>>).postValue(usersList)
                usersNameMap.clear()
                usersNameMap.putAll(usersList.filter { it.username != null}.associateBy{ it.username!!})
            }catch (e: Exception){
                Log.e("SearchViewModelFetchUsers", e.message.toString())
            }
        }

    }

    fun fetchUserData(userId: String){
        launch {
            val userDocRef = db.collection("users").document(userId)
            try {
                val snapshot = userDocRef.get().await()

                snapshot?.let { documentSnapshot ->
                    val user = documentSnapshot.toObject(Users::class.java)
                    currencyUser.postValue(user!!)
                }
            } catch (e: Exception) {
                Log.e("SearchViewModelFetchCurrencyUser",e.message.toString())
            }
        }
    }

}