package com.example.couturecorner.cart.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.R
import com.example.couturecorner.authentication.viewmodel.LoginViewModel
import com.example.couturecorner.data.model.Address
import com.example.couturecorner.data.model.Amount
import com.example.couturecorner.data.model.ExperienceContext
import com.example.couturecorner.data.model.OrderRequest
import com.example.couturecorner.data.model.PayPalExperience
import com.example.couturecorner.data.model.PaymentSource
import com.example.couturecorner.data.model.PurchaseUnit
import com.example.couturecorner.data.repository.PayPalRepository
import com.example.couturecorner.databinding.FragmentCheckOutBinding
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.cart.viewmodel.CartViewModel
import com.example.couturecorner.cart.viewmodel.CheckOutViewModel
import com.example.couturecorner.cart.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class CheckOutFragment : Fragment() {
    private var isTokenFetched = false

    private val checkOutViewModel: CheckOutViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val userViewModel:UserViewModel by viewModels()
private val cartViewModel: CartViewModel by viewModels()
    private lateinit var binding: FragmentCheckOutBinding
    private lateinit var payPalRepository: PayPalRepository

    // PayPal settings
    private val returnUrl = "com.example.couturecorner://paypalreturn"
    private val cancelUrl = "https://example.com/cancelUrl"
    private var orderId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckOutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomNav()

        loginViewModel.getCustomerDataTwo()
        setupCustomerAddress()
        cartViewModel.getCartItems()
        observeViewModel()

        binding.btnAddNewAddress.setOnClickListener {
            findNavController().navigate(R.id.action_checkOutFragment_to_addAdressFragment)
        }
binding.checkOutButton.setOnClickListener {
    initializePayPal()
}
        binding.radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.pay_with_paypal -> {
                    initializePayPal()
                    paypall()
                    Toast.makeText(requireContext(), "Pay with PayPal selected", Toast.LENGTH_SHORT).show()
                }
                R.id.cash_on_delivery -> {
                    Toast.makeText(requireContext(), "Cash on Delivery selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun observeViewModel() {
        // Observe cart items
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->

            cartViewModel.createDraftOrder(cartItems)

        }
    }

        private fun setupCustomerAddress() {
            userViewModel.userData.observe(viewLifecycleOwner) { user ->
                user?.let {
                    if (user.defaultAddress != null) {
                        binding.curretAdress.visibility = View.VISIBLE
                        binding.addressName.text = it.defaultAddress?.address1
                        binding.addressDetails.text = it.defaultAddress?.address2
                        val address = Address(
                            name = it.defaultAddress?.address1 ?: "",
                            addressDetails = it.defaultAddress?.address2 ?: "",
                            city = it.defaultAddress?.city ?: "",
                            phone = it.defaultAddress?.phone ?: "",

                            )
                        Log.i("Final", "setupCustomerAddress: "+address)
                        cartViewModel.setAddress(address)

                    }
                }
            }

        }

    private fun initializePayPal() {
        payPalRepository = PayPalRepository(
            clientID = "AQSo4-8c09dCj6c-SU8c_5dmxfpDeOqkgoRwqmI80ZxNYMuwciCLnf6k1z_X2niaNNwHPyA67OuUxQBl",
            secretID = "ECe4BSmb5wSs57wrq-hN94TJRurRrovSPfjJqRmq1Sxdm40sx6vPU--vZIA1xXQSBSOB5nEZ3obHtKjG"
        )
        fetchAccessToken()
    }

    private fun fetchAccessToken() {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Coroutine exception: ${throwable.localizedMessage}")
            Toast.makeText(requireContext(), "Error: ${throwable.localizedMessage}", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch(handler) {
            try {
                val token = payPalRepository.fetchAccessToken()
                isTokenFetched = true
                Toast.makeText(requireContext(), "Access Token Fetched!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to fetch access token: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun paypall() {

        if (isTokenFetched) {
            startOrder()
        } else {
            Toast.makeText(requireContext(), "Please fetch the access token first.", Toast.LENGTH_SHORT).show()
        }
        }


    private fun startOrder() {
        val uniqueId = UUID.randomUUID().toString()
        val orderRequest = OrderRequest(
            purchase_units = listOf(
                PurchaseUnit(
                    reference_id = uniqueId,
                    amount = Amount(currency_code = "USD", value = "5.00")
                )
            ),
            payment_source = PaymentSource(
                paypal = PayPalExperience(
                    experience_context = ExperienceContext(
                        payment_method_preference = "IMMEDIATE_PAYMENT_REQUIRED",
                        brand_name = "Couture-Corner",
                        locale = "en-US",
                        landing_page = "LOGIN",
                        shipping_preference = "NO_SHIPPING",
                        user_action = "PAY_NOW",
                        return_url = returnUrl,
                        cancel_url = cancelUrl
                    )
                )
            )
        )

        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Coroutine exception: ${throwable.localizedMessage}")
            Toast.makeText(requireContext(), "Error creating order: ${throwable.localizedMessage}", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch(handler) {
            try {
                val response = payPalRepository.createOrder(orderRequest)
                orderId = response.id
                val approvalLink = response.links.get(1).href

                if (!approvalLink.isNullOrEmpty()) {
                    Log.i(TAG, "Approval Link: $approvalLink")
                    openApprovalUrl(approvalLink)
                } else {
                    Log.e(TAG, "Approval link not found in the response.")
                    Toast.makeText(requireContext(), "Approval link not found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error creating order: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openApprovalUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    companion object {
        const val TAG = "PayPalExample"
    }
}
