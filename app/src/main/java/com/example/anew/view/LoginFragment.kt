package com.example.anew.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.anew.R
import com.example.anew.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        setupButtons()
    }

    private fun setupButtons(){
        binding.buttonLogin.setOnClickListener {
            binding.progressBarLogin.visibility = View.VISIBLE
            performLogin()
        }
        binding.textViewRegister.setOnClickListener {
           findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        binding.buttonForgetPass.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
        }
    }
    private fun performLogin() {
        val email = binding.editTextLoginInfo.text.toString()
        val pass = binding.editTextPassword.text.toString()

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val verification = auth.currentUser?.isEmailVerified
                    if (verification == true) {
                        saveFcnToken()
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        binding.progressBarLogin.visibility = View.GONE
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        binding.progressBarLogin.visibility = View.GONE
                        showToast("Please verify your Email!")
                    }
                } else {
                    binding.progressBarLogin.visibility = View.GONE
                    showToast("Login failed!")
                }
            }

            .addOnFailureListener { e ->
                binding.progressBarLogin.visibility = View.GONE
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
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }
}