package com.example.couturecorner.setting.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.content.Intent
import android.content.SharedPreferences
import com.example.couturecorner.R
import com.example.couturecorner.authentication.view.LoginActivity

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val logoutButton: Button = view.findViewById(R.id.button)
        logoutButton.setOnClickListener {
            logoutUser()
        }

        return view
    }

    private fun logoutUser() {
        sharedPreferences.edit().remove("isLoggedIn").apply()

        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
