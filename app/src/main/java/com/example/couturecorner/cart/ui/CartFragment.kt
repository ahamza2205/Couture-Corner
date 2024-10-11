package com.example.couturecorner.cart.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.databinding.FragmentCartBinding
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.cart.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private val cartViewModel: CartViewModel by viewModels()
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
        cartViewModel.getCartItems()

        observeViewModel()
        setupUI()

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
        binding.textViewDiscountValue.text = "$5.00"
    }

    // Observe LiveData changes from ViewModel
    private fun observeViewModel() {
        // Observe cart items
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->

            cartItemAdapter.updateCartItems(cartItems)
            cartItemAdapter.notifyDataSetChanged()  // Notify adapter to refresh data
        }

        // Observe subtotal
        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            binding.textViewSubtotalValue.text = "$${String.format("%.2f", subtotal)}"
        }

        // Observe total price
        cartViewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            binding.textViewTotalValue.text = "$${String.format("%.2f", total)}"
        }

        // Observe cart update status (API response handling)
        cartViewModel.updateCartStatus.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { cartItems ->
                    cartItemAdapter.updateCartItems(cartItems)
                },
                onFailure = { exception ->
                    Log.e("CartFragment", "Error fetching cart items", exception)
                    Toast.makeText(requireContext(), "Error fetching cart items", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}