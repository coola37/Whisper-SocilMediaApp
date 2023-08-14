package com.example.anew.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.anew.R
import com.example.anew.model.UserDetails
import com.example.anew.model.Users
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextUserame: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button

    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()

        buttonRegister.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                performRegister()
            }
        }
    }

    private fun initializeViews() {
        editTextUserame = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextTextPassword2)
        buttonRegister = findViewById(R.id.buttonRegister)
    }

    private suspend fun performRegister() {
        val email = editTextEmail.text.toString()
        val username = "@${editTextUserame.text.toString()}"
        val password = editTextPassword.text.toString()

        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.sendEmailVerification()?.await()

            saveDb(authResult.user?.uid.toString(), username, email)

            Toast.makeText(
                this,
                "You can log in after verifying your account with the link sent to your e-mail address.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "The user could not be created.", Toast.LENGTH_SHORT).show()
            Log.e("performRegister", e.toString())
        }
    }

    private suspend fun saveDb(uId: String, username: String, email: String) {
        val usersCollection = db.collection("users")
        val userId = auth.uid.toString()
        val detail = UserDetails("", "")
        val user = Users(uId, username, email, detail)

        try {
            usersCollection.document(userId).set(user).await()
            Log.e("saveDb", "successful")
        } catch (e: Exception) {
            Log.e("saveDb", e.toString())
        }
    }
}