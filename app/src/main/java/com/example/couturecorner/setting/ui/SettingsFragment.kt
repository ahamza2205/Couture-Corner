package com.example.couturecorner.setting.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.couturecorner.R
import com.example.couturecorner.databinding.FragmentSettingsBinding
import com.example.couturecorner.setting.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.setting.viewmodel.CurrencyViewModel
import com.example.couturecorner.authentication.view.LoginActivity

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val currencyViewModel: CurrencyViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

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

                Toast.makeText(requireContext(), "Currency set to $selectedCurrency", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        return binding.root
    }

}

