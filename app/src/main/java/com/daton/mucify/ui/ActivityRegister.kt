package com.daton.mucify.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daton.mucify.databinding.ActivityRegisterBinding
import com.daton.user.User

class ActivityRegister : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            User.register(
                binding.editEmail.text.toString(),
                binding.editPassword.text.toString()
            ) { result ->
                if (!result.isSuccessful)
                    Toast.makeText(
                        this,
                        "No Register: ${result.exception!!.message}",
                        Toast.LENGTH_LONG
                    ).show()

                onBackPressed()
                finish()
            }
        }

        binding.txtLogin.setOnClickListener {
            startActivity(Intent(this, ActivitySignIn::class.java))
            finish()
        }
    }
}