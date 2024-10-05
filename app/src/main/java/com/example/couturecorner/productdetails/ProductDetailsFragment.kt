package com.example.couturecorner.productdetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.home.viewmodel.HomeViewModel
import com.example.couturecorner.home.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private val homeviewModel: MainViewModel by activityViewModels()
    private val viewModel: ProductDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_product_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the selected product ID from the ViewModel
        val productId = homeviewModel.selectedProductId ?: return

        // Fetch product details
        viewModel.getProductDetails(productId)

        // Observe product details state
        lifecycleScope.launch {
            viewModel.productDetails.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // Show loading indicator
                    }
                    is ApiState.Success -> {
                        // Update UI with product details
                        val product = state.data?.product
                        // Set product data to views here
                    }
                    is ApiState.Error -> {
                        // Show error message
                    }
                }
            }
        }
    }
}
