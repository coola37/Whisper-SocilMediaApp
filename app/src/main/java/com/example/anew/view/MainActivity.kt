package com.example.anew.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.anew.R
import com.example.anew.utils.BottomNavigationViewHelper
import com.example.anew.utils.NetworkConnection
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    internal lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var bottomNavigationView: BottomNavigationView
    private val ACTIVITY_NO = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottomNavigationView3)
        setupAuthListener()
        networkCheck()
        setupNavigationView()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        authListener?.let {
            auth.removeAuthStateListener(authListener)
        }
    }

    private fun networkCheck(){
        val networkConnection = NetworkConnection(applicationContext)
        lifecycleScope.launch{
            networkConnection.observe(this@MainActivity, Observer{ isConnected->
                if(isConnected){
                    Log.d("Network Check", "OK")
                }

                else{
                    val mAlerDiaglog = MaterialAlertDialogBuilder(this@MainActivity)
                    mAlerDiaglog.setTitle("INTERNET NOT FOUND")
                    mAlerDiaglog.setMessage("Check your internet connection!")
                    mAlerDiaglog.setIcon(R.mipmap.ic_launcher)
                    mAlerDiaglog.setPositiveButton("Try again!"){dialog, i-> networkCheck()}
                    mAlerDiaglog.setNegativeButton("Exit"){dialog, i-> finish()}
                    mAlerDiaglog.show()
                }
            })
        }
    }
   private fun setupAuthListener(){
       lifecycleScope.launch {
           authListener = object : FirebaseAuth.AuthStateListener{
               override fun onAuthStateChanged(p0: FirebaseAuth) {
                   val user: FirebaseUser? = auth.currentUser
                   user?.let {
                       return
                   }?: kotlin.run {
                       val intent = Intent(this@MainActivity, LoginActivity::class.java)
                       startActivity(intent)
                       finish()
                   }
               }
           }
       }
   }

    private fun setupNavigationView() {
        BottomNavigationViewHelper.setupNavigation(this, bottomNavigationView)
        val menu = bottomNavigationView.menu
        val menuItem = menu.getItem(ACTIVITY_NO)
        menuItem.isChecked = true
    }
}