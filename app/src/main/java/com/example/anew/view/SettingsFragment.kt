package com.example.anew.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.anew.R
import com.example.anew.databinding.FragmentSettingsBinding
import com.example.anew.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    @Inject
    internal lateinit var auth: FirebaseAuth
    @Inject
    internal lateinit var db: FirebaseFirestore
    @Inject
    internal lateinit var glide: RequestManager
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        setupButtonClick()
    }

    private fun setupButtonClick(){
        binding.imageViewBack.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.textViewChangeEmail.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_changeEmailFragment)
        }
        binding.textViewChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_changePasswordFragment)
        }
        binding.textViewSettingsSignOut.setOnClickListener {
            auth.signOut()
        }
    }
}