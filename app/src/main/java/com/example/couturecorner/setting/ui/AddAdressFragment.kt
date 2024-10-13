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
import com.example.couturecorner.setting.viewmodel.UserViewModel
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.setting.viewmodel.AddAdressViewModel
import com.graphql.type.MailingAddressInput
@AndroidEntryPoint
class AddAdressFragment : Fragment() {

    private var _binding: FragmentAddAdressBinding? = null
    private val binding get() = _binding!!

    private val viewModelAddAdress: AddAdressViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAdressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomNav()

        // Observe the update status LiveData
        viewModelAddAdress.updateStatus.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { customerId ->
                Toast.makeText(requireContext(), "Address added successfully", Toast.LENGTH_LONG)
                    .show()
                userViewModel.getCustomerData()

            }.onFailure { exception ->
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })



        binding.btnConfirmAddress.setOnClickListener {
            val addressName = binding.addressName.text.toString()
            val addressDetails = binding.detailsOfShippingAddress.text.toString()
            val city = binding.city.text.toString()
            val phoneNumber = binding.phoneNumber.text.toString()

            // Validate inputs
            var isValid = true

            if (addressName.isEmpty()) {
                binding.addressNameform.error = "Please enter address name"
                isValid = false
            } else {
                binding.addressNameform.error = null
            }

            if (addressDetails.isEmpty()) {
                binding.detailsOfShippingAddressform.error =
                    "Please enter details of the shipping address"
                isValid = false
            } else {
                binding.detailsOfShippingAddressform.error = null
            }

            if (city.isEmpty()) {
                binding.cityform.error = "Please enter city"
                isValid = false
            } else {
                binding.cityform.error = null
            }

            if (phoneNumber.isEmpty()) {
                binding.phoneNumberform.error = "Please enter phone number"
                isValid = false
            } else {
                binding.phoneNumberform.error = null
            }

            if (isValid) {
                val newAddress = MailingAddressInput(
                    address1 = addressName,
                    address2 = addressDetails,
                    city = city,
                    phone = phoneNumber
                )

                userViewModel.userData.observe(viewLifecycleOwner) { user ->
                    user?.let {
                        val addressList: List<MailingAddressInput>

                        if (user.defaultAddress == null) {
                            // No default address, send only the new address
                            addressList = listOf(newAddress)
                            viewModelAddAdress.updateAddressCustomer(addressList, user.id)
                            Log.i("AddAddressFragment", "Sent new address: $addressList")
                        } else {
                            // Default address exists, add new address to the list
                            val existingAddresses = user.addresses ?: emptyList()
                            addressList = existingAddresses.map {
                                MailingAddressInput(
                                    address1 = it?.address1 ?: "",
                                    address2 = it?.address2 ?: "",
                                    city = it?.city ?: "",
                                    phone = it?.phone ?: ""
                                )
                            } + newAddress // Append the new address to the list

                            viewModelAddAdress.updateAddressCustomer(addressList, user.id)
                            Log.i(
                                "AddAddressFragment",
                                "Appended new address to existing: $addressList"
                            )

                        }
                    }
                }
            }

            binding.detailsOfShippingAddressform.setOnClickListener {
            }
        }
    }


        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
