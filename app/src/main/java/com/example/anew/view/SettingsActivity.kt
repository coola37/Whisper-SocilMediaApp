package com.example.anew.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.anew.R
import com.example.anew.utils.NetworkConnection
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    internal lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupAuthListener()
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
            networkConnection.observe(this@SettingsActivity, Observer{ isConnected->
                if(isConnected){
                    Log.d("Network Check", "OK")
                }

                else{
                    val mAlerDiaglog = MaterialAlertDialogBuilder(this@SettingsActivity)
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
                        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}