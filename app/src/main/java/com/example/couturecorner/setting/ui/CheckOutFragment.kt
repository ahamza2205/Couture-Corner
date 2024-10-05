package com.example.couturecorner.setting.ui
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.R
import com.example.couturecorner.databinding.FragmentCheckOutBinding


class CheckOutFragment : Fragment() {

lateinit var binding: FragmentCheckOutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckOutBinding.inflate(inflater, container, false)

        return binding.root
    }
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.btnAddNewAddress.setOnClickListener {
        findNavController().navigate(R.id.action_checkOutFragment_to_addAdressFragment)
    }
}

}