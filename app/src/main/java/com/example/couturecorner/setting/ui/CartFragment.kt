package com.example.couturecorner.setting.ui
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.adapter.CartItemAdapter
import com.example.couturecorner.databinding.FragmentCartBinding
import com.example.couturecorner.home.ui.MainActivity
import com.example.couturecorner.setting.viewmodel.CartViewModel
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
        observeViewModel()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomNav()

cartItemAdapter = CartItemAdapter()
        binding.cartRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartItemAdapter
        cartViewModel.getCartItems()

//        // Observe changes to the cart items from the ViewModel
//        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
//            cartItemAdapter.updateCartItems(cartItems)
//        })
//
//        cartViewModel.totalPrice.observe(viewLifecycleOwner, Observer { total ->
//            binding.textViewTotalValue.text = "$${String.format("%.2f", total)}"
//        })
//        cartViewModel.subtotal.observe(viewLifecycleOwner, Observer { subtotal ->
//            binding.textViewSubtotalValue.text = "$${String.format("%.2f", subtotal)}"
//        })
        binding.textViewDeliveryFeeValue.text = "$5.00"
        binding.textViewDiscountValue.text= "$5.00"
binding.applyButton.setOnClickListener {
    findNavController().navigate(R.id.action_cartFragment_to_checkOutFragment)

}

    }

    private fun observeViewModel() {
        cartViewModel.uppdatCartListStatus.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { cartItems ->
                    cartItemAdapter.updateCartItems(cartItems)
                },
                onFailure = { exception ->

                    // Handle error (e.g., show a toast or a snack bar)
                    Log.e("YourFragment", "Error fetching cart items", exception)
                }
            )
        }
    }
}

