package com.example.couturecorner.authentication.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.couturecorner.authentication.viewmodel.SignUpViewModel
import com.example.couturecorner.databinding.ActivitySignUpBinding
import com.example.couturecorner.home.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textView2.paintFlags = binding.textView2.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.textView2.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
       observeViewModel()
    }
    private fun registerUser() {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        if (firstName.isNotEmpty() && lastName.isNotEmpty() && phoneNumber.isNotEmpty() &&
            email.isNotEmpty() && password.isNotEmpty()
        ) {
            viewModel.registerUser(email, password, firstName, lastName, phoneNumber)
            Toast.makeText(this, "Registration in progress...", Toast.LENGTH_SHORT).show()
        }
    }
    private fun observeViewModel() {
        viewModel.registrationStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                binding.root.postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1500)
            } else {
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}




