package com.example.couturecorner.setting.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.setting.viewmodel.UserViewModel
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
    lateinit var addressAdapter: AddressCardAdapter
    private var defaultAddress: Address? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressAdapter = AddressCardAdapter(this)

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
                defaultAddress = user.defaultAddress?.let { apiAddress ->
                    Address(
                        name = apiAddress.address1 ?: "",
                        addressDetails = apiAddress.address2 ?: "",
                        city = apiAddress.city ?: "",
                        phone = apiAddress.phone ?: ""
                    )
                }

                val addressList = user.addresses?.map { apiAddress ->
                    Address(
                        name = apiAddress?.address1 ?: "",
                        addressDetails = apiAddress?.address2 ?: "",
                        city = apiAddress?.city ?: "",
                        phone = apiAddress?.phone ?: ""
                    )
                } ?: emptyList()

                if (addressList.isEmpty()) {
                    binding.progressBar2.visibility = View.GONE
                    binding.recyclerViewAddress.visibility = View.GONE
                    binding.textView4.visibility = View.VISIBLE
                    binding.imageView3.visibility = View.VISIBLE
                } else {
                    binding.progressBar2.visibility = View.GONE
                    binding.textView4.visibility = View.GONE
                    binding.imageView3.visibility = View.GONE
                    binding.recyclerViewAddress.visibility = View.VISIBLE

                    addressAdapter.setAddressList(addressList)
                    Log.i("AddressFragment", "Address list set: $addressList")
                }
            }
        }
    }

    override fun onDeleteAddress(address: Address) {
        if (address == defaultAddress) {
            showCannotDeleteDialog()
        } else {

            DialogUtils.showCustomDialog(
                context = requireContext(),
                message = "Do you want to delete this Address?",
                positiveButtonText = "Yes",
                negativeButtonText = "No",
                lottieAnimationResId = R.raw.login,
                positiveAction = {
                    userViewModel.userData.observe(viewLifecycleOwner) { user ->
                        user?.let {
                            val existingAddresses = user.addresses?.toMutableList() ?: mutableListOf()

                            val updatedAddresses = existingAddresses.filter {
                                it?.address1 != address.name || it?.address2 != address.addressDetails
                            }

                            val addressList = updatedAddresses.map {
                                MailingAddressInput(
                                    address1 = it?.address1 ?: "",
                                    address2 = it?.address2 ?: "",
                                    city = it?.city ?: "",
                                    phone = it?.phone ?: ""
                                )
                            }

                            addressViewModel.updateAddressCustomer(addressList, user.id)

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
                                binding.recyclerViewAddress.visibility = View.GONE
                                binding.textView4.visibility = View.VISIBLE
                                binding.imageView3.visibility = View.VISIBLE
                            }

                            Log.i("AddressFragment", "Updated address list sent to API: $addressList")

                            Toast.makeText(requireContext(), "Address deleted", Toast.LENGTH_SHORT).show()
                        }
                    }




                    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
                },
                negativeAction = {
                    Toast.makeText(requireContext(), "Item not deleted", Toast.LENGTH_SHORT).show()
                }
            )


        }
    }

    private fun showCannotDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Default Address")
            .setMessage("The default address cannot be deleted.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}