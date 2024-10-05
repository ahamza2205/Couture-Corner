package com.example.couturecorner.authentication.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignUp.setOnClickListener {
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
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSignUp.setOnClickListener {
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val phoneNumber = binding.etPhoneNumber.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && phoneNumber.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty()
            ) {
                viewModel.registerUser(
                    email,
                    password,
                    firstName,
                    lastName,
                    phoneNumber
                )
                Toast.makeText(this, "Registration in progress...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.registrationStatus.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                // Fetch customer data after successful registration
                viewModel.getCustomerData()

                // Observe the customer data to log it
                viewModel.customerData.observe(this) { customer ->
                    if (customer != null) {
                        Log.d("HamzaData", "Customer Data: " +
                                "ID: ${customer.id}, " +
                                "Display Name: ${customer.displayName}, " +
                                "Email: ${customer.email}, " +
                                "First Name: ${customer.firstName}, " +
                                "Last Name: ${customer.lastName}, " +
                                "Phone: ${customer.phone}, " +
                                "Created At: ${customer.createdAt}, " +
                                "Updated At: ${customer.updatedAt}")

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("HamzaData", "Customer data is null")
                    }
                }
            } else {
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT)
                    .show()
            }
        })



    }
}

