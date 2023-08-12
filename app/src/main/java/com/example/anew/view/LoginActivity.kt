package com.example.anew.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.anew.R

class LoginActivity : AppCompatActivity() {

    private lateinit var textViewRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        textViewRegister = findViewById(R.id.textViewRegister)

        setupButtonClick()

    }

    private fun setupButtonClick(){
        textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }

    }
}