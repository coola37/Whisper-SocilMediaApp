package com.example.anew.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.anew.R
import com.example.anew.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentChangePasswordBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChangePasswordBinding.bind(view)
        binding.buttonChangePassword.setOnClickListener {
            changePassword()
        }
        binding.imageVirwBack.setOnClickListener {
            findNavController().navigate(R.id.action_changePasswordFragment_to_settingsFragment)
        }
    }

    private fun changePassword() {
        val user = auth.currentUser
        val newPassword = binding.editTextTextPasswordChange.text.toString()
        val newPassword2 = binding.editTextTextPasswordChange2.text.toString()
        val password = binding.editTextTextPasswordChangePassword.text.toString()
        if (newPassword == newPassword2) {
            val credential = EmailAuthProvider.getCredential(user?.email!!, password)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireActivity(), "Password change successful.", Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().onBackPressed()
                            } else {
                                Toast.makeText(requireActivity(), "Password change failed!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Log.e("CurrentUserVerification", task.exception.toString())
                }
            }
        } else {
            Toast.makeText(requireActivity(), "The passwords you entered do not match!.", Toast.LENGTH_SHORT).show()
        }
    }
}
