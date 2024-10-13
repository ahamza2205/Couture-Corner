package com.example.couturecorner.setting.ui
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentCartBinding
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.home.viewmodel.CuponsVeiwModel
import com.example.couturecorner.setting.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private val cartViewModel: CartViewModel by viewModels()
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



        binding.textCuponcode.setOnEditorActionListener { textView, actionId, event ->
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
            ondeleteItem = { cartItem ->
                cartViewModel.onDeleteCartItem(cartItem)
            }
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
                    val cartItems = apiState.data // This is the List<CartItem>
                    cartItemAdapter.updateCartItems(cartItems!!)
                    cartItemAdapter.notifyDataSetChanged()  // Notify adapter to refresh data

                    // Show or hide the empty cart image based on the cart items
                    if (cartItems.isEmpty()) {
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
            binding.textViewSubtotalValue.text = "$${String.format("%.2f", subtotal)}"
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { subtotal ->
            binding.textViewTotalValue.text = "$${String.format("%.2f", subtotal)}"
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
                    cartItemAdapter.updateCartItems(cartItems!!)
                    cartItemAdapter.notifyDataSetChanged()  // Notify adapter to refresh data

                    // Show or hide the empty cart image based on the cart items
                    if (cartItems.isEmpty()) {
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

    }
}