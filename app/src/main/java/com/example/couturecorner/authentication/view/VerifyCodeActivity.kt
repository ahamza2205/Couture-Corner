package com.example.couturecorner.authentication.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.couturecorner.authentication.viewmodel.SignUpViewModel
import com.example.couturecorner.databinding.ActivityVerifyCodeBinding
import com.example.couturecorner.home.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyCodeActivity : AppCompatActivity() {
    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityVerifyCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnCheckVerification.setOnClickListener {
            auth.currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser?.isEmailVerified == true) {
                        Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                        viewModel.saveUserLoggedIn(true)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Please verify your email before proceeding.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnResendVerification.setOnClickListener {
            auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent again. Please check your inbox.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send verification email. Please try again later.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
