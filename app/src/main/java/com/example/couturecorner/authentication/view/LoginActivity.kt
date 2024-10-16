package com.example.couturecorner.authentication.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.couturecorner.R
import com.example.couturecorner.authentication.viewmodel.LoginViewModel
import com.example.couturecorner.authentication.viewmodel.RegistrationState
import com.example.couturecorner.databinding.ActivityLoginBinding
import com.example.couturecorner.home.ui.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        setupGoogleSignIn()
        setupListeners()
        checkUserLoggedIn()
    }

    private fun setupListeners() {
        binding.loginBtnGoogle.setOnClickListener {
            signInUsingGoogle()
        }

        // Underline the Sign Up text
        binding.textView2.paintFlags = binding.textView2.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.textView2.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.loginBtnGuest.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBtnSignIn.setOnClickListener {
            handleSignIn()
        }

        // Observe login status
        viewModel.loginStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                navigateToMainActivity()
            } else {
                showToast("Error logging in as guest")
            }
        }

        observeViewModel()
    }

    private fun handleSignIn() {
        val email = binding.loginEtEmail.text.toString().trim()
        val password = binding.loginEtPassword.text.toString().trim()

        if (isValidEmail(email) && isValidPassword(password)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        fetchCustomerData(email)
                    } else {
                        showToast("Error: ${task.exception?.message}")
                    }
                }
        } else {
            showToast("Please enter a valid email and password.")
        }
    }

    private fun fetchCustomerData(email: String) {
        viewModel.getCustomerDataFromFirebaseAuth(email)
        viewModel.customerData.observe(this) { customer ->
            customer?.let {
                Log.d("CustomerData", it.toString())
                viewModel.saveUserLoggedIn(true)
                navigateToMainActivity()
            } ?: run {
                showToast("Unable to fetch customer data.")
            }
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun checkUserLoggedIn() {
        if (viewModel.isUserLoggedIn()) {
            navigateToMainActivity()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInUsingGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { data ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                handleGoogleAccount(it)
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign-in failed: ${e.message}")
            showToast("Sign-in failed: ${e.message}")
        }
    }

    private fun handleGoogleAccount(account: GoogleSignInAccount) {
        val email = account.email ?: ""
        val idToken = account.idToken ?: ""

        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email == email) {
            Log.d("GoogleSignIn", "User is already signed in, skipping Shopify registration.")
            navigateToMainActivity()
            return
        }
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result.signInMethods

                if (!signInMethods.isNullOrEmpty()) {
                    Log.d("GoogleSignIn", "User already registered in Firebase. Skipping Shopify registration.")
                    signInWithFirebase(idToken)
                    navigateToMainActivity()
                } else {
                    Log.d("GoogleSignIn", "No sign-in methods found for $email. Proceeding with Shopify registration.")
                    registerNewUserWithGoogle(email, idToken)
                }
            } else {
                Log.e("GoogleSignIn", "Failed to fetch sign-in methods: ${task.exception?.message}")
                showToast("Failed to check if user is registered: ${task.exception?.message}")
            }
        }
    }

    private fun registerNewUserWithGoogle(email: String, idToken: String) {
        Log.d("GoogleSignIn", "Registering new user with Google: $email")

        val nameBeforeAt = email.split("@")[0]
        val firstName: String
        val lastName: String

        if (nameBeforeAt.contains(".") || nameBeforeAt.contains("_")) {
            val nameParts = nameBeforeAt.split(Regex("[._]"))
            firstName = nameParts.getOrElse(0) { "" }.replaceFirstChar { it.uppercase() }
            lastName = if (nameParts.size > 1) {
                nameParts[1].replaceFirstChar { it.uppercase() }
            } else {
                ""
            }
        } else {
            firstName = nameBeforeAt.take(4).replaceFirstChar { it.uppercase() }
            lastName = nameBeforeAt.drop(4).replaceFirstChar { it.uppercase() }
        }

        viewModel.registerUserWithGoogle(email, null, firstName, lastName, null, idToken)
        viewModel.registrationStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                Log.d("GoogleSignIn", "Successfully registered user with Shopify: $email")
                signInWithFirebase(idToken)
            } else {
                Log.e("GoogleSignIn", "Failed to register user with Shopify: $email")
                showToast("Shopify registration failed.")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.registrationStatus.observe(this) { registrationState ->
            if (registrationState) {
                showToast("Registration successful!")
                binding.root.postDelayed({ navigateToMainActivity() }, 1500)
            } else {
                showToast("Registration failed. Please try again.")
            }
        }
    }
    private fun signInWithFirebase(idToken: String) {
        if (idToken.isNotEmpty()) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("GoogleSignIn", "Successfully signed in with Firebase.")

                        val user = auth.currentUser
                        user?.let {
                            if (!it.isEmailVerified) {
                                it.sendEmailVerification().addOnCompleteListener { verifyTask ->
                                    if (verifyTask.isSuccessful) {
                                        showToast("Verification email sent to ${it.email}")
                                        Log.d("EmailVerification", "Verification email sent.")
                                    } else {
                                        showToast("Failed to send verification email.")
                                        Log.e("EmailVerification", "Error: ${verifyTask.exception?.message}")
                                    }
                                }
                            } else {
                                navigateToMainActivity()
                            }
                        }
                    } else {
                        Log.e("GoogleSignIn", "Firebase sign-in failed: ${task.exception?.message}")
                        showToast("Failed to sign in with Firebase: ${task.exception?.message}")
                    }
                }
        } else {
            Log.e("GoogleSignIn", "Invalid idToken. Cannot sign in with Firebase.")
            showToast("Invalid idToken.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6 // You can add more validation logic here if needed
    }
}
