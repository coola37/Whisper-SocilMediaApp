package com.example.anew.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.anew.R
import com.example.anew.databinding.FragmentRegisterBinding
import com.example.anew.model.UserDetails
import com.example.anew.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentRegisterBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        binding.buttonRegister.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressBarRegister.visibility = View.VISIBLE
                performRegister()
            }
        }
    }

    private suspend fun performRegister() {
        val email = binding.editTextEmail.text.toString()
        val username = "@${binding.editTextUsername.text}"
        val password = binding.editTextTextPassword2.text.toString()

        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.sendEmailVerification()?.await()

            saveDb(authResult.user?.uid.toString(), username, email)

            Toast.makeText(
                requireActivity(),
                "You can log in after verifying your account with the link sent to your e-mail address.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            binding.progressBarRegister.visibility = View.GONE
            startActivity(intent)
            requireActivity().finish()

        } catch (e: Exception) {
            binding.progressBarRegister.visibility = View.GONE
            Toast.makeText(requireActivity(), "The user could not be created.", Toast.LENGTH_SHORT).show()
            Log.e("performRegister", e.toString())
        }
    }

    private suspend fun saveDb(uId: String, username: String, email: String) {
        val usersCollection = db.collection("users")
        val userId = auth.uid.toString()
        val detail = UserDetails(binding.editTextUsername.text.toString(), "", 0, 0, "")
        val user = Users(uId, username, email, detail)

        try {
            usersCollection.document(userId).set(user).await()
            Log.e("saveDb", "successful")
        } catch (e: Exception) {
            Log.e("saveDb", e.toString())
            Toast.makeText(
                requireActivity(),
                e.message.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}