package com.example.anew.utils

import android.content.Context
import android.content.Intent
import com.example.anew.R
import com.example.anew.view.InboxActivity
import com.example.anew.view.MainActivity
import com.example.anew.view.NotificationsActivity
import com.example.anew.view.SearchActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationViewHelper {
    companion object{
        fun setupNavigation(context: Context, bottomNavigationView: BottomNavigationView){
            bottomNavigationView.setOnItemSelectedListener {
                when (it.itemId) {

                    R.id.ic_action_home -> {
                        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                        true
                    }
                    R.id.ic_action_search -> {
                        val intent = Intent(context, SearchActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                        true
                    }
                    R.id.ic_action_notification -> {
                        val intent = Intent(context, NotificationsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                        true
                    }
                    R.id.ic_action_inbox -> {
                        val intent = Intent(context, InboxActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                        true
                    }

                    else -> false
                }
            }
        }
    }
}