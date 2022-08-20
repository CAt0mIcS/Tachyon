package com.daton.mucify.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daton.mucify.databinding.ActivitySignInBinding
import com.daton.user.User

class ActivitySignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            User.signIn(
                binding.editEmail.text.toString(),
                binding.editPassword.text.toString()
            ) { result ->
                if (!result.isSuccessful)
                    Toast.makeText(
                        this,
                        "No Login: ${result.exception!!.message}",
                        Toast.LENGTH_LONG
                    ).show()

                onBackPressed()
                finish()
            }
        }

        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, ActivityRegister::class.java))
            finish()
        }
    }
}