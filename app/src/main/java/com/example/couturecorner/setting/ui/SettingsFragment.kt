package com.example.couturecorner.setting.ui
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.R
import com.example.couturecorner.databinding.FragmentSettingsBinding
class SettingsFragment : Fragment() {
    lateinit var  binding : FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentSettingsBinding.inflate(inflater, container, false)

        binding.cartLayout.setOnClickListener{
            Log.i("Settings", "onCreateView: "+"clicked")
            findNavController().navigate(R.id.action_settingsFragment_to_cartFragment)

        }
        return binding.root

    }


}