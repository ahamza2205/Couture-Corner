package com.example.couturecorner.setting.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import com.example.couturecorner.R
import com.example.couturecorner.databinding.FragmentSettingsBinding
import com.example.couturecorner.setting.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.setting.viewmodel.CurrencyViewModel
import com.example.couturecorner.authentication.view.LoginActivity
import com.example.couturecorner.data.local.SharedPreferenceImp

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val currencyViewModel: CurrencyViewModel by viewModels({ requireActivity() })
    private lateinit var sharedPreference: SharedPreferenceImp

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize SharedPreference
        sharedPreference = SharedPreferenceImp(requireContext())

        // Check if the user is a guest
        if (isUserGuest()) {
            showLoginRequiredDialog()
        } else {
            setupUI() // Setup UI if the user is not a guest
        }

        return binding.root
    }

    private fun isUserGuest(): Boolean {
        val isLoggedIn = sharedPreference.isUserLoggedIn()
        Log.d("UserStatus", "User is logged in: $isLoggedIn")
        return !isLoggedIn // User is a guest if not logged in
    }

    private fun setupUI() {
        binding.logoutLayout.setOnClickListener {
            settingsViewModel.logoutUser()
            Toast.makeText(requireContext(), "You have successfully logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.cartLayout.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_cartFragment)
        }
        binding.orderLayout.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_ordersFragment)
        }
        binding.addressLayout.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_adressFragment)
        }

        val currencies = arrayOf("USD", "EUR", "EGP", "SAR", "AED")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter

        binding.currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency = currencies[position]
                currencyViewModel.saveSelectedCurrency(selectedCurrency)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed
                // Handle the case where nothing is selected if needed
            }
        }

        val savedCurrency = currencyViewModel.getSelectedCurrency()

        val savedCurrencyPosition = currencies.indexOf(savedCurrency)

        if (savedCurrencyPosition != -1) {
            binding.currencySpinner.setSelection(savedCurrencyPosition)
        }

//        return binding.root
    }

    private fun showLoginRequiredDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_login_required, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.loginButton).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // Hide all the UI elements in the fragment since the user is a guest
        binding.logoutLayout.visibility = View.GONE
        binding.cartLayout.visibility = View.GONE
        binding.orderLayout.visibility = View.GONE
        binding.addressLayout.visibility = View.GONE
        binding.currencySpinner.visibility = View.GONE
        binding.profileImageView.visibility = View.GONE
        binding.divider.visibility = View.GONE
        binding.divider2.visibility = View.GONE
        binding.divider3.visibility = View.GONE
        binding.divider4.visibility = View.GONE
        binding.currencyLayout.visibility = View.GONE
        binding.nameTextView.visibility = View.GONE
    }
}
