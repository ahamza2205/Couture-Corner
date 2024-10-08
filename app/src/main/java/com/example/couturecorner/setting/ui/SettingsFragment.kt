package com.example.couturecorner.setting.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.couturecorner.R
import com.example.couturecorner.databinding.FragmentSettingsBinding
import com.example.couturecorner.setting.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.authentication.view.LoginActivity

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
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
        return binding.root
    }
}

