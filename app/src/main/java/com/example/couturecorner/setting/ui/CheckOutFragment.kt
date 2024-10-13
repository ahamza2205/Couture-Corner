package com.example.couturecorner.setting.ui
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.authentication.viewmodel.LoginViewModel
import com.example.couturecorner.data.model.Address
import com.example.couturecorner.data.model.Amount
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.model.ExperienceContext
import com.example.couturecorner.data.model.OrderRequest
import com.example.couturecorner.data.model.PayPalExperience
import com.example.couturecorner.data.model.PaymentSource
import com.example.couturecorner.data.model.PurchaseUnit
import com.example.couturecorner.data.repository.PayPalRepository
import com.example.couturecorner.databinding.FragmentCheckOutBinding
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.setting.viewmodel.CartViewModel
import com.example.couturecorner.setting.viewmodel.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class CheckOutFragment : BottomSheetDialogFragment() , OnAddressClickListener {
    private var isTokenFetched = false
    private val loginViewModel: LoginViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
private val cartViewModel: CartViewModel by viewModels()
    private lateinit var binding: FragmentCheckOutBinding
    private lateinit var payPalRepository: PayPalRepository
    private lateinit var addressItemAdapter: AddressItemAdapter

    // PayPal settings
    private val returnUrl = "com.example.couturecorner.setting.ui.settings://paypalreturn"
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
initializePayPal()
        loginViewModel.getCustomerDataTwo()
        setupCustomerAddress()
        cartViewModel.getCartItems()


addressItemAdapter = AddressItemAdapter(this)
        binding.recyclerViewAddress.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewAddress.adapter = addressItemAdapter


        binding.btnAddNewAddress.setOnClickListener {
            this.dismiss()
            findNavController().navigate(R.id.action_cartFragment_to_addAdressFragment)
        }

        binding.radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.pay_with_paypal -> {
                    paypall()
                    Toast.makeText(requireContext(), "Pay with PayPal selected", Toast.LENGTH_SHORT).show()
                }
                R.id.cash_on_delivery -> {
                    Toast.makeText(requireContext(), "Cash on Delivery selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.ensureOrderButton.setOnClickListener {
            observeViewModel()
        }
    }


    private fun observeViewModel() {
        // Observe cart items
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->

            cartViewModel.createDraftOrder(cartItems)
        }
        cartViewModel.draftOrderStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ApiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT).show()
                    //made confirm dalog here
                    //navigate to order screen
                    this.dismiss()
                    findNavController().navigate(
                        R.id.action_cartFragment_to_ordersFragment,                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.cartFragment, true)  // This ensures the cartFragment is removed from the backstack
                            .build()
                    )
                }
                is ApiState.Error -> {
                    // Show error message in UI
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is ApiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }


    }

        private fun setupCustomerAddress() {
            userViewModel.userData.observe(viewLifecycleOwner) { user ->
                user?.let {
                    if (user.addresses != null) {
                        val addressList = user.addresses?.map { apiAddress ->
                            Address(
                                name = apiAddress?.address1 ?: "",
                                addressDetails = apiAddress?.address2 ?: "",
                                city = apiAddress?.city ?: "",
                                phone = apiAddress?.phone ?: ""
                            )
                        } ?: emptyList()

                        addressItemAdapter.setAddressItems(addressList)



                    }
                }
            }

        }
    override fun onAddressClick(address: Address) {
        Log.i("onAddressClick", "onAddressClick: "+address)

        cartViewModel.setAddress(address)

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
