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
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.setting.viewmodel.CartViewModel
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding

    private val cartViewModel: CartViewModel by viewModels()
    private val sharedViewModel:MainViewModel by activityViewModels()

    private lateinit var cartItemAdapter: CartItemAdapter

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
        setupRecyclerView()
        setupUI()
        observeViewModel()

binding.applyButton.visibility=View.VISIBLE
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
            ondeleteItem={ cartItem ->
                cartViewModel.onDeleteCartItem(cartItem)
            },
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
                }
                is ApiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val cartItems = apiState.data // This is the List<CartItem>
                    prepareProductsForAdapter(cartItems?: emptyList())
//                    cartItemAdapter.updateCartItems(cartItems!!)
//                    cartItemAdapter.notifyDataSetChanged()  // Notify adapter to refresh data
                }
                is ApiState.Error -> {
                    // Handle error state
                    Log.e("CartFragment", "Error fetching cart items: ")
                    Toast.makeText(requireContext(), "Error fetching cart items", Toast.LENGTH_SHORT).show()
                }
            }
      // Notify adapter to refresh data
        }

        // Observe subtotal
        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
          //  binding.textViewSubtotalValue.text = "$${String.format("%.2f", subtotal)}"
            sharedViewModel.convertCurrency("EGP",sharedViewModel.getSelectedCurrency() ?: "EGP",
                subtotal,"subTotal")
        }

        // Observe total price
        cartViewModel.totalPrice.observe(viewLifecycleOwner) { total ->
          //  binding.textViewTotalValue.text = "$${String.format("%.2f", total)}"
            sharedViewModel.convertCurrency("EGP",sharedViewModel.getSelectedCurrency() ?: "EGP",
                total,"total")
        }

        lifecycleScope.launch {
            sharedViewModel.convertedCurrency.collect { conversions ->
                binding.textViewSubtotalValue.text = getString(
                    R.string.price,
                    conversions["subTotal"].toString(),
                    sharedViewModel.getSelectedCurrency() ?: "EGP"
                )
                binding.textViewTotalValue.text =getString(
                    R.string.price,
                    conversions["total"].toString(),
                    sharedViewModel.getSelectedCurrency() ?: "EGP"
                )
//                binding.textViewSubtotalValue.text = "$${String.format("%.2f", conversions["subTotal"])}"
//                binding.textViewTotalValue.text = "$${String.format("%.2f", conversions["total"])}"
            }
        }

// Observe cart update status
        cartViewModel.updateCartStatus.observe(viewLifecycleOwner) { result ->
            when(result) {
                is ApiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                }
                is ApiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val cartItems = result.data // This is the List<CartItem>
                    prepareProductsForAdapter(cartItems?: emptyList())
//                    cartItemAdapter.updateCartItems(cartItems!!)
//                    cartItemAdapter.notifyDataSetChanged()  // Notify adapter to refresh data
                }
                is ApiState.Error -> {
                    // Handle error state
                    Log.e("CartFragment", "Error fetching cart items: ")
                    Toast.makeText(requireContext(), "Error fetching cart items", Toast.LENGTH_SHORT).show()
                }
            }
        }

            }


    private fun prepareProductsForAdapter(cartItems: List<CartItem>) {
        if (cartItems.isNotEmpty()) {
            val updatedProducts = cartItems.map { item ->
                val productId = item.id
                val originalPrice = item.price?.toDoubleOrNull() ?: 0.0

                // Trigger conversion for each product
                sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", originalPrice, productId ?: "")

                // Return the original product, the price will be updated later
                item
            }

            // Observe currency conversion updates
            lifecycleScope.launch {
                sharedViewModel.convertedCurrency.collect { conversions ->
                    val updatedList = updatedProducts.map { item ->
                        val productId = item.id
                        val convertedPrice =
                            conversions[productId] ?: item.price?.toDoubleOrNull() ?: 0.0

                        val price = item.price?.toDoubleOrNull()
                        // Create a copy or modify the product item with the new price
                        item.copy(
                            price = convertedPrice.toString()
                        )


                        // Submit the updated list with converted prices to the adapter
                    }
                    cartItemAdapter.updateCartItems(updatedList)
                    cartItemAdapter.notifyDataSetChanged()
                }
            }
            }
            else
            {
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

