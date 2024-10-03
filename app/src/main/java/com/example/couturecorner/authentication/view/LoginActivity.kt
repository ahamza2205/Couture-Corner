package com.example.couturecorner.authentication.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.couturecorner.R
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.authentication.viewmodel.LoginViewModel
import com.example.couturecorner.databinding.ActivityLoginBinding
import com.example.couturecorner.home.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.textView2.paintFlags = binding.textView2.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        // Set onClickListener to navigate to SignInActivity
        binding.textView2.setOnClickListener {
            // Start SignInActivity when the text is clicked
            val intent = Intent(this, SignUpActivity::class.java) // Replace with your actual SignInActivity
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()

        binding.loginBtnSignIn.setOnClickListener { // Use binding to access the button
            val email = binding.loginEtEmail.text.toString() // Use binding to access the email EditText
            val password = binding.loginEtPassword.text.toString() // Use binding to access the password EditText


            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModel.saveUserLoggedIn(true) // Save login state
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        if (viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java)) // Example navigation
            finish()
        }
    }
}
