package com.example.anew.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.anew.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var textViewRegister: TextView
    private lateinit var editTextLoginInfo: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    @Inject
    internal lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupButtons()

    }

    private fun initializeViews() {
        textViewRegister = findViewById(R.id.textViewRegister)
        editTextLoginInfo = findViewById(R.id.editTextLoginInfo)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
    }
    private fun setupButtons(){
        buttonLogin.setOnClickListener {
            performLogin()
        }
        textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    private fun performLogin() {
        val email = editTextLoginInfo.text.toString()
        val pass = editTextPassword.text.toString()

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val verification = auth.currentUser?.isEmailVerified
                    if (verification == true) {
                        saveFcnToken()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showToast("Please verify your Email!")
                    }
                } else {
                    showToast("Login failed!")
                }
            }

            .addOnFailureListener { e ->
                Log.e("LoginError", e.toString())
            }
    }

    private fun saveFcnToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                saveNewTokenInFirestore(token)
            }
        }
    }

    private fun saveNewTokenInFirestore(newToken: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            userRef.update("fcn_token", newToken)
                .addOnSuccessListener {
                    Log.e("saveNewTokenInFirestore", "successful")
                }
                .addOnFailureListener { e ->
                    Log.e("saveNewTokenInFirestore", e.toString())
                }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
