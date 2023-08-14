package com.example.anew.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.anew.R
import com.example.anew.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setupBottomNavigationView()
        setupButtonClick()

    }


    private fun setupBottomNavigationView(){
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {

            when(it.itemId){

                R.id.ic_action_search -> {
                    Log.e("search","click")
                    findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
                    false
                }
                R.id.ic_action_notification -> {
                    Log.e("notification","click")
                    findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment)
                    false
                }
                R.id.ic_action_inbox -> {
                    Log.e("inbox","click")
                    findNavController().navigate(R.id.action_homeFragment_to_inboxFragment)
                    false
                }

                else -> false
            }
        }
        val menuItem = binding.bottomNavigationView.menu.getItem(0)
        menuItem.isChecked = true
    }
    private fun setupButtonClick(){
        binding.circleImage.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }
}
