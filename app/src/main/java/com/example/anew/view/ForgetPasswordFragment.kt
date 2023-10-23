package com.example.anew.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.anew.R
import com.example.anew.databinding.FragmentForgerPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForgetPasswordFragment : Fragment(R.layout.fragment_forger_password) {

    @Inject
    internal lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentForgerPasswordBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentForgerPasswordBinding.bind(view)
        binding.button.setOnClickListener {
            val emailAddress = binding.editTextTextEmailAddress.text.toString()
            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.editTextTextEmailAddress.text.clear()
                        Toast.makeText(requireActivity(), "Your password has been sent to your e-mail address.",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireActivity(), task.exception?.message.toString(),
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}