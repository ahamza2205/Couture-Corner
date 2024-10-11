package com.example.couturecorner.setting.ui
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.cart.viewmodel.UserViewModel
import com.example.couturecorner.data.model.Address
import com.example.couturecorner.databinding.FragmentAdressBinding
import com.example.couturecorner.setting.viewmodel.AddAdressViewModel
import com.graphql.type.MailingAddressInput
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class AdressFragment : Fragment(), OnAddressDeleteListener {
    lateinit var binding: FragmentAdressBinding
    val userViewModel: UserViewModel by viewModels()
    val addressViewModel: AddAdressViewModel by viewModels()
    lateinit var addressAdapter: AddressAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressAdapter = AddressAdapter(this)

        binding.recyclerViewAddress.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewAddress.adapter = addressAdapter

        binding.btnAddNewAddress.setOnClickListener {
            findNavController().navigate(R.id.action_adressFragment_to_addAdressFragment)
        }

        // Load the address list initially
        reloadAddressList()
    }
    override fun onResume() {
        super.onResume()
        reloadAddressList()
    }

    private fun reloadAddressList() {
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                val addressList = user.addresses?.map { apiAddress ->
                    Address(
                        name = apiAddress?.address1 ?: "",
                        addressDetails = apiAddress?.address2 ?: "",
                        city = apiAddress?.city ?: "",
                        phone = apiAddress?.phone ?: ""
                    )
                } ?: emptyList()

                if (addressList.isEmpty()) {
                    // Show textView and imageView when the list is empty
                    binding.progressBar2.visibility = View.GONE
                    binding.recyclerViewAddress.visibility = View.GONE
                    binding.textView4.visibility = View.VISIBLE
                    binding.imageView3.visibility = View.VISIBLE
                } else {
                    // Hide textView and imageView when there are addresses
                    binding.progressBar2.visibility = View.GONE
                    binding.textView4.visibility = View.GONE
                    binding.imageView3.visibility = View.GONE
                    binding.recyclerViewAddress.visibility = View.VISIBLE

                    // Set the addresses in the adapter
                    addressAdapter.setAddressList(addressList)
                    Log.i("AddressFragment", "Address list set: $addressList")
                }
            }
        }
    }

    override fun onDeleteAddress(address: Address) {
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                val existingAddresses = user.addresses?.toMutableList() ?: mutableListOf()

                // Remove the selected address
                val updatedAddresses = existingAddresses.filter {
                    it?.address1 != address.name || it?.address2 != address.addressDetails
                }

                // Prepare the list to send to the API
                val addressList = updatedAddresses.map {
                    MailingAddressInput(
                        address1 = it?.address1 ?: "",
                        address2 = it?.address2 ?: "",
                        city = it?.city ?: "",
                        phone = it?.phone ?: ""
                    )
                }

                // Send the updated address list to the API
                addressViewModel.updateAddressCustomer(addressList, user.id)

                // Update the adapter with the new address list
                addressAdapter.setAddressList(
                    updatedAddresses.map { apiAddress ->
                        Address(
                            name = apiAddress?.address1 ?: "",
                            addressDetails = apiAddress?.address2 ?: "",
                            city = apiAddress?.city ?: "",
                            phone = apiAddress?.phone ?: ""
                        )
                    }
                )

                if (updatedAddresses.isEmpty()) {
                    // Show textView4 and imageView3 when the list is empty after deletion
                    binding.recyclerViewAddress.visibility = View.GONE
                    binding.textView4.visibility = View.VISIBLE
                    binding.imageView3.visibility = View.VISIBLE
                }

                Log.i("AddressFragment", "Updated address list sent to API: $addressList")

                // Provide feedback to the user
                Toast.makeText(requireContext(), "Address deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
