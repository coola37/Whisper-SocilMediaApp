package com.example.anew.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.anew.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextUserame: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextVerifyCode: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var textViewGetCode: TextView
    private lateinit var buttonVerify: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonRegister: Button

    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextUserame = findViewById(R.id.editTextUsername)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextVerifyCode = findViewById(R.id.editTextVerifyCode)
        editTextPassword = findViewById(R.id.editTextTextPassword2)
        textViewGetCode = findViewById(R.id.textViewGetCode)
        buttonVerify = findViewById(R.id.buttonVerify)
        buttonRegister = findViewById(R.id.buttonRegister)

        auth = FirebaseAuth.getInstance()



        textViewGetCode.setOnClickListener {
            sendVerificationCode(editTextPhone.text.toString())
        }

        buttonVerify.setOnClickListener {
            if(editTextVerifyCode.text.toString().isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId!!, editTextVerifyCode.text.toString())
                signInWithPhoneAuthCredential(credential)
            }
        }



        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                    Log.e("TAG", "onVerificationCompleted:$credential")


            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.e("TAG", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }



    }



    private fun sendVerificationCode(number: String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Log.e("TAG", "signInWithCredential:success")

                    buttonRegister.isEnabled = true
                    var textView: TextView = findViewById(R.id.textViewVerify)
                    textView.visibility = View.VISIBLE
                    val user = task.result?.user


                } else {
                    // Sign in failed, display a message and update the UI
                    Log.e("TAG", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
}