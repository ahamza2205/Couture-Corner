package com.example.couturecorner.authentication.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.R
import com.example.couturecorner.authentication.viewmodel.SignUpViewModel
import com.example.couturecorner.databinding.ActivitySignUpBinding
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.network.ApolloClient
import com.google.firebase.auth.FirebaseAuth
import com.graphql.CreateCustomerMutation
import com.graphql.GetCustomerQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding // Binding instance for view binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView2.paintFlags = binding.textView2.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        // Set onClickListener to navigate to SignInActivity
        binding.textView2.setOnClickListener {
            // Start SignInActivity when the text is clicked
            val intent = Intent(this, LoginActivity::class.java) // Replace with your actual SignInActivity
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString() // Get email input
            val password = binding.etPassword.text.toString() // Get password input


            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.saveUserLoggedIn(true) // Save login state

                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                createShopifyUser(email)
                            }
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createShopifyUser(email: String) {
        val client = ApolloClient.apolloClient

        val userId = auth.currentUser?.uid ?: return

        // Create mutation to add the customer to Shopify
        val mutation = CreateCustomerMutation(
            email = email,
            firstName = "Abdulrahman",
            lastName = "Hamza",
            tags = Optional.Present(listOf(userId)) // Passing Firebase userId as Shopify tag
        )

        // Launch a coroutine to execute the mutation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.mutation(mutation).execute()

                withContext(Dispatchers.Main) {
                    if (response.hasErrors()) {
                        Log.e("Shopify", "Error creating user: ${response.errors}")
                    } else {
                        val shopifyUserId = response.data?.customerCreate?.customer?.id
                        Log.d("Shopify", "User created successfully in Shopify: $shopifyUserId")

                        // Log both Firebase and Shopify User IDs for comparison
                        Log.d("User Comparison", "Firebase User ID: $userId, Shopify User ID: $shopifyUserId")

                        // Ensure both User IDs are logged properly
                        Log.d("User Info", "Firebase ID: $userId")
                        Log.d("User Info", "Shopify ID: $shopifyUserId")
                        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                        finish()
                        // Fetch the customer from Shopify to verify
                    // fetchShopifyCustomer(email)
                    }
                }
            } catch (e: Exception) {
                Log.e("Shopify", "Mutation failed: ${e.message}")
            }
        }
    }

/*    // Function to fetch a customer from Shopify using their email
    private fun fetchShopifyCustomer(email: String) {
        val query = GetCustomerQuery(email = email) // Create query for fetching customer

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApolloClient.apolloClient.query(query).execute() // Execute the query

                withContext(Dispatchers.Main) {
                    if (response.hasErrors()) {
                        Log.e("Shopify", "Error fetching customer: ${response.errors?.joinToString { it.message }}")
                    } else {
                        val customer = response.data?.customerByEmail // Get customer data
                        if (customer != null) {
                            Log.d("Shopify", "Customer found: ${customer.email}")
                            Log.d("Shopify", "Customer Tags: ${customer.tags}")

                            if (customer.tags!!.contains(auth.currentUser?.uid)) {
                                Log.d("Shopify", "Firebase User ID is linked successfully!")


                            } else {
                                Log.d("Shopify", "Firebase User ID is NOT linked.")
                            }
                        } else {
                            Log.d("Shopify", "No customer found with that email.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Shopify", "Query failed: ${e.message}")
            }
        }
    }*/
}
