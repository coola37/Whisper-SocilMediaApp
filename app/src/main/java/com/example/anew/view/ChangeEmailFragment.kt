package com.example.anew.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.anew.R
import com.example.anew.databinding.FragmentChangeEmailBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeEmailFragment : Fragment(R.layout.fragment_change_email) {

    private lateinit var binding: FragmentChangeEmailBinding
    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChangeEmailBinding.bind(view)

        binding.buttonChangePassword.setOnClickListener {
            changeEmail()
        }
    }

    private fun changeEmail(){
        val user = auth.currentUser
        val password = binding.editTextTextPasswordChangePassword.text.toString()
        val email = binding.editTextTextPasswordChange.text.toString()
        val email2 = binding.editTextTextPasswordChange2.text.toString()

        if(email == email2){
            val credential = EmailAuthProvider.getCredential(user?.email!!, password)

            user.reauthenticate(credential)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        user.updateEmail(email).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(requireContext(), "Email replacement was succesfull", Toast.LENGTH_SHORT).show()
                                requireActivity().onBackPressed()
                            }else{
                                Toast.makeText(requireContext(), "Email replacement failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(requireContext(), "Password is wrong, please check.", Toast.LENGTH_SHORT).show()
                    }
                }
        }else{
            Toast.makeText(requireContext(), "The emails you entered do not match.", Toast.LENGTH_SHORT).show()
        }
    }
}