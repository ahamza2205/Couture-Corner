package com.example.couturecorner.setting.ui
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.couturecorner.databinding.FragmentAddAdressBinding
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.R
import com.example.couturecorner.authentication.viewmodel.LoginViewModel
import com.example.couturecorner.setting.viewmodel.AddAdressViewModel
import com.graphql.type.MailingAddressInput

@AndroidEntryPoint
class AddAdressFragment : Fragment() {

    private var _binding: FragmentAddAdressBinding? = null
    private val binding get() = _binding!!

    private val viewModelAddAdress: AddAdressViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAdressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.getCustomerDataTwo()

        // Observe the update status LiveData
        viewModelAddAdress.updateStatus.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { customerId ->

                Toast.makeText(requireContext(), "Customer Updated: $customerId", Toast.LENGTH_LONG).show()
                viewModelAddAdress.saveAddressState()
                findNavController().navigate(R.id.action_addAdressFragment_to_checkOutFragment)

            }.onFailure { exception ->
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        })

        binding.btnConfirmAddress.setOnClickListener {


                val addressList = listOf(
                    MailingAddressInput(
                        address1 = binding.addressName.text.toString(),
                        address2 = binding.detailsOfShippingAddress.text.toString(),
                        city = binding.city.text.toString(),
                        phone = binding.phoneNumber.text.toString()
                    )
                )
            loginViewModel.customerData.observe(viewLifecycleOwner){customer->

                if (customer != null) {
                    Log.i("ADDRESSSS", "onViewCreated: "+customer.id)
                    viewModelAddAdress.updateCustomer(addressList,customer.id)

                }
                Log.i("ADDRESSSS", "onViewCreated: "+"${customer?.defaultAddress?.address1}+NOTTT")

            }


        }
        binding.detailsOfShippingAddressform.setOnClickListener(
            {
                findNavController().navigate(R.id.action_addAdressFragment_to_mapFragment)

            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




