package com.example.couturecorner.setting.ui
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.couturecorner.databinding.FragmentAddAdressBinding


class AddAdressFragment : Fragment() {

lateinit var binding: FragmentAddAdressBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAddAdressBinding.inflate(inflater, container, false)
return binding.root


    }


}