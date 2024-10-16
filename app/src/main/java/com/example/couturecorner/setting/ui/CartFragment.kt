package com.example.couturecorner.setting.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var cartItemAdapter: CartItemAdapter
    private val cuponsVeiwModel: CuponsVeiwModel by viewModels()
    private val sharedViewModel: MainViewModel by activityViewModels()

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




        binding.textCuponcode.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val userInput = textView.text.toString()
                cuponsVeiwModel.validateCouponCode(userInput)
                true
            } else {
                false
            }
        }

//        binding.swipeRefreshLayout.setOnRefreshListener {
//            observeViewModel()
//        }


    }


    private fun observeCouponFetching() {
        lifecycleScope.launchWhenStarted {
            cuponsVeiwModel.cupons.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                    }

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
                if (validationResult != null) {
                    if (validationResult.contains("Invalid")) {
                        binding.textInputLayout.error = "Please enter a valid coupon code"
                        Toast.makeText(context, validationResult, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.i("coupon", "observeCupons: " + validationResult)

                        cartViewModel.setDiscount(validationResult.toDouble())
                        binding.textViewDiscountValue.text = "$validationResult % "
                        Toast.makeText(context, "Success: $validationResult", Toast.LENGTH_SHORT)
                            .show()
                        binding.textInputLayout.error = null
                        binding.textInputLayout.helperText = "Coupon applied successfully!"
                        // Disable the coupon code input and set the text
                        binding.textCuponcode.isEnabled = false
                        binding.textCuponcode.setText("You used a coupon already")
                    }
                }
            }
        }
    }


    // Set up RecyclerView for cart items
    private fun setupRecyclerView() {
        cartItemAdapter = CartItemAdapter(
            onIncreaseQuantity = { cartItem ->
                cartViewModel.increaseQuantity(cartItem)
            },
            onDecreaseQuantity = { cartItem ->
                cartViewModel.decreaseQuantity(cartItem)
            },

            ondeleteItem = { cartItem,lamda ->
                    Dialog.showCustomDialog(
                        context = requireContext(),
                        message = "Do you want to delete this Product From Cart?",
                        positiveButtonText = "Yes",
                        negativeButtonText = "No",
                        lottieAnimationResId = R.raw.warning,
                        positiveAction = {
                            lamda.invoke()
                            cartViewModel.onDeleteCartItem(cartItem)
                            Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
                         },
                         negativeAction = {
                            Toast.makeText(requireContext(), "Item not deleted", Toast.LENGTH_SHORT).show()
                        }
                    ) },
            getCurrency = {getCurrencySymbol(sharedViewModel.getSelectedCurrency()?: "EGP")}
        )
        binding.cartRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartItemAdapter
    }

    // Set up button clicks and UI elements
    private fun setupUI() {
        binding.applyButton.setOnClickListener {
            val checkOutFragment = CheckOutFragment()
            checkOutFragment.show(parentFragmentManager, "CheckOutFragment")
        }

    }

    // Observe LiveData changes from ViewModel

    private fun observeViewModel() {
        // Observe cart items
        cartViewModel.cartItems.observe(viewLifecycleOwner) { apiState ->
            when (apiState) {
                is ApiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyCartImageView.visibility =
                        View.GONE // Hide the empty cart image while loading
                }

                is ApiState.Success -> {
                    binding.progressBar.visibility = View.GONE
//                    binding.swipeRefreshLayouteshLayout.isRefreshing = false
                    val cartItems = apiState.data // This is the List<CartItem>
                    prepareProductsForAdapter(cartItems?: emptyList())
//                    cartItemAdapter.updateCartItems(cartItems!!)
//                    cartItemAdapter.notifyDataSetChanged()  // Notify adapter to refresh data

                    // Show or hide the empty cart image based on the cart items
                    if (cartItems?.isEmpty() == true) {
                        binding.emptyCartImageView.visibility =
                            View.VISIBLE // Show empty cart image
                    } else {
                        binding.emptyCartImageView.visibility = View.GONE // Hide empty cart image
                    }
                }

                is ApiState.Error -> {
                    // Handle error state
                    Log.e("CartFragment", "Error fetching cart items: ")
                    Toast.makeText(
                        requireContext(),
                        "Error fetching cart items",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.emptyCartImageView.visibility =
                        View.VISIBLE // Show empty cart image if there's an error
                }
            }
        }

        // Observe subtotal
        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", subtotal, "subTotal")
//            binding.textViewSubtotalValue.text = "$${String.format("%.2f", subtotal)}"
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { subtotal ->
            sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", subtotal, "total")
//            binding.textViewTotalValue.text = "$${String.format("%.2f", subtotal)}"
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

        // Observe cart update status
        cartViewModel.updateCartStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is ApiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val cartItems = result.data // This is the List<CartItem>


                    prepareProductsForAdapter(cartItems?: emptyList())
//                    cartItemAdapter.updateCartItems(cartItems!!)
//                    cartItemAdapter.notifyDataSetChanged()  // Notify adapter to refresh data

                    // Show or hide the empty cart image based on the cart items

                        if (cartItems?.isEmpty() == true) {
                            binding.emptyCartImageView.visibility =
                                View.VISIBLE // Show empty cart image
                        } else {
                            binding.emptyCartImageView.visibility = View.GONE // Hide empty cart image
                        }

                }

                is ApiState.Error -> {
                    // Handle error state
                    Log.e("CartFragment", "Error fetching cart items: ")
                    Toast.makeText(
                        requireContext(),
                        "Error fetching cart items",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.emptyCartImageView.visibility =
                        View.VISIBLE
                }
            }
        }
        cartViewModel.showInventoryExceededDialog.observe(viewLifecycleOwner) { cartItem ->
            showCannotDeleteDialog()
        }

    }
    private fun showCannotDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Out of Stock")
            .setMessage("you cannot added more than that out of stock")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }






    private fun prepareProductsForAdapter(cartItems: List<CartItem>) {
     //   if (cartItems.isNotEmpty()) {
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
                }
            }
//        } else {
//            cartItemAdapter.updateCartItems(cartItems)
//        }
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