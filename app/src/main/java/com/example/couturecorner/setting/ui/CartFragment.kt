package com.example.couturecorner.setting.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.model.CartItem
import com.example.couturecorner.databinding.FragmentCartBinding
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.home.viewmodel.CuponsVeiwModel
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.setting.viewmodel.CartViewModel
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding

    private val cartViewModel: CartViewModel by viewModels()
    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var cartItemAdapter: CartItemAdapter
    private val cuponsVeiwModel: CuponsVeiwModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomNav()
        observeViewModel()
        setupRecyclerView()
        setupUI()
        cuponsVeiwModel.getCupons()
        observeCouponFetching()

        binding.textCuponcode.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val userInput = textView.text.toString()
                cuponsVeiwModel.validateCouponCode(userInput)
                true
            } else {
                false
            }
        }
    }

    private fun observeCouponFetching() {
        lifecycleScope.launchWhenStarted {
            cuponsVeiwModel.cupons.collect { state ->
                when (state) {
                    is ApiState.Loading -> { }
                    is ApiState.Success -> {
                        observeCupons()
                    }
                    is ApiState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error fetching coupons: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun observeCupons() {
        lifecycleScope.launchWhenStarted {
            cuponsVeiwModel.validationResult.collect { validationResult ->
                validationResult?.let {
                    if (it.contains("Invalid")) {
                        binding.textInputLayout.error = "Please enter a valid coupon code"
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.i("coupon", "observeCupons: $it")
                        cartViewModel.setDiscount(it.toDouble())
                        binding.textViewDiscountValue.text = "$it %"
                        Toast.makeText(context, "Success: $it", Toast.LENGTH_SHORT).show()
                        binding.textInputLayout.error = null
                        binding.textInputLayout.helperText = "Coupon applied successfully!"
                        binding.textCuponcode.apply {
                            isEnabled = false
                            setText("You used a coupon already")
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        cartItemAdapter = CartItemAdapter(
            onIncreaseQuantity = { cartItem ->
                cartViewModel.increaseQuantity(cartItem)
            },
            onDecreaseQuantity = { cartItem ->
                cartViewModel.decreaseQuantity(cartItem)
            },
            ondeleteItem = { cartItem ->
                cartViewModel.onDeleteCartItem(cartItem)
            },
            getCurrency = {
                getCurrencySymbol(sharedViewModel.getSelectedCurrency() ?: "EGP")
            }
        )
        binding.cartRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartItemAdapter
    }

    private fun setupUI() {
        binding.applyButton.setOnClickListener {
            val checkOutFragment = CheckOutFragment()
            checkOutFragment.show(parentFragmentManager, "CheckOutFragment")
        }
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { apiState ->
            when (apiState) {
                is ApiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyCartImageView.visibility = View.GONE
                }
                is ApiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val cartItems = apiState.data ?: emptyList()
                    prepareProductsForAdapter(cartItems)
                    binding.emptyCartImageView.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
                }
                is ApiState.Error -> {
                    Log.e("CartFragment", "Error fetching cart items")
                    Toast.makeText(
                        requireContext(),
                        "Error fetching cart items",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.emptyCartImageView.visibility = View.VISIBLE
                }
            }
        }

        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", subtotal, "subTotal")
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", total, "total")
        }

        lifecycleScope.launch {
            sharedViewModel.convertedCurrency.collect { conversions ->
                binding.textViewSubtotalValue.text = getString(
                    R.string.price,
                    conversions["subTotal"].toString(),
                    sharedViewModel.getSelectedCurrency() ?: "EGP"
                )
                binding.textViewTotalValue.text = getString(
                    R.string.price,
                    conversions["total"].toString(),
                    sharedViewModel.getSelectedCurrency() ?: "EGP"
                )
            }
        }

        cartViewModel.updateCartStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ApiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val cartItems = result.data ?: emptyList()
                    prepareProductsForAdapter(cartItems)
                    binding.emptyCartImageView.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
                }
                is ApiState.Error -> {
                    Log.e("CartFragment", "Error fetching cart items")
                    Toast.makeText(
                        requireContext(),
                        "Error fetching cart items",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.emptyCartImageView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun prepareProductsForAdapter(cartItems: List<CartItem>) {
        if (cartItems.isNotEmpty()) {
            val updatedProducts = cartItems.map { item ->
                val productId = item.id
                val originalPrice = item.price?.toDoubleOrNull() ?: 0.0
                sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", originalPrice, productId ?: "")
                item
            }

            lifecycleScope.launch {
                sharedViewModel.convertedCurrency.collect { conversions ->
                    val updatedList = updatedProducts.map { item ->
                        val productId = item.id
                        val convertedPrice = conversions[productId] ?: item.price?.toDoubleOrNull() ?: 0.0
                        item.copy(price = convertedPrice.toString())
                    }
                    cartItemAdapter.updateCartItems(updatedList)
                    cartItemAdapter.notifyDataSetChanged()
                }
            }
        } else {
            cartItemAdapter.updateCartItems(cartItems)
            cartItemAdapter.notifyDataSetChanged()
        }
    }

    fun getCurrencySymbol(currency: String): String {
        return when (currency) {
            "USD" -> "$"
            "EUR" -> "€"
            "EGP" -> "EGP"
            "SAR" -> "SAR"
            "AED" -> "AED"
            else -> ""
        }
    }
}
