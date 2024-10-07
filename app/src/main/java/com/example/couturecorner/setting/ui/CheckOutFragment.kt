package com.example.couturecorner.setting.ui
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.R
import com.example.couturecorner.authentication.viewmodel.LoginViewModel
import com.example.couturecorner.databinding.FragmentCheckOutBinding
import com.example.couturecorner.setting.viewmodel.CheckOutViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckOutFragment : Fragment() {
    val checkOutViewModel: CheckOutViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()


    lateinit var binding: FragmentCheckOutBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckOutBinding.inflate(inflater, container, false)

        return binding.root
    }
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    loginViewModel.getCustomerDataTwo()
    Log.i("ADDRESS", "onViewCreated: "+"${checkOutViewModel.getAddressState()}")
    if(checkOutViewModel.getAddressState()==true){
        binding.curretAdress.visibility=View.VISIBLE
        loginViewModel.customerData.observe(viewLifecycleOwner){customer->

            if (customer != null) {
                Log.i("ADDRESSSS", "onViewCreated: "+customer.id)
                binding.addressName.text=customer.defaultAddress?.address1
                binding.addressDetails.text=customer.defaultAddress?.address2
            }
            Log.i("ADDRESSSS", "onViewCreated: "+"${customer?.defaultAddress?.address1}+NOTTT")

            }
    }

    binding.btnAddNewAddress.setOnClickListener {
        findNavController().navigate(R.id.action_checkOutFragment_to_addAdressFragment)
    }
}

}