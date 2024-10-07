package com.example.couturecorner.productdetails.view

import android.R // Use the correct library
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log // Import Log class
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentProductDetailsBinding
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.productdetails.viewmodel.ProductDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: ProductDetailsViewModel by viewModels()

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagesAdapter: ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the product ID from arguments
        val args = ProductDetailsFragmentArgs.fromBundle(requireArguments())
        val productIdFromArgs = args.productId

        val productId: String = if (productIdFromArgs.isNotEmpty()) {
            productIdFromArgs
        } else {
            mainViewModel.selectedProductId ?: return
        }

        // Fetch product details
        viewModel.getProductDetails(productId)

        // Observe product details state
        lifecycleScope.launch {
            viewModel.productDetails.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // Show loading indicator
                        Log.d("ProductDetailsFragment", "Loading product details...")
                    }

                    is ApiState.Success -> {
                        val product = state.data?.product
                        Log.d("ProductDetailsFragment", "Product details received: $product")

                        // Update UI with product details
                        product?.let {
                            binding.productTitle.text = it.title

                            val colorFromTitle = it.title.split("|").lastOrNull()?.trim()

                            val availableSizesWithPrices = it.variants?.edges?.mapNotNull { variant ->
                                variant?.node?.let { node ->
                                    val price = node.price
                                    val color = node.displayName?.split(" - ")?.get(1)?.trim() ?: "Not specified"
                                    "($color) - Price: ${price}"
                                }
                            } ?: listOf()

                            // Log the available sizes with prices for debugging
                            availableSizesWithPrices.forEach { sizeWithPrice ->
                                Log.d("ProductDetailsFragment", "Available Size: $sizeWithPrice")
                            }

                            // Update UI components
                            binding.productDescription.text = it.description
                            binding.priceValue.text = "${it.variants?.edges?.get(0)?.node?.price}"
                            binding.productTypeValue.text = it.productType
                            binding.stockCount.text = "${it.totalInventory} items available"

                            // Set up image carousel
                            it.images?.edges?.let { imageEdges ->
                                setupImagesRecyclerView(imageEdges.map { imageEdge -> imageEdge?.node?.src ?: "" })
                            }

                            // Set up size spinner
                            setupSpinner(binding.sizesSpinner, availableSizesWithPrices, null)

                            // Set up color spinner with one color only
                            val colors = listOf(colorFromTitle ?: "Not specified")
                            setupSpinner(binding.colorsSpinner, colors, null)
                        }
                    }

                    is ApiState.Error -> {
                        Log.e("ProductDetailsFragment", "Error fetching product details: ${state.message}")
                    }
                }
            }
        }
        // Handle add to cart action
        binding.btnAddToCart.setOnClickListener {
            val selectedSize = binding.sizesSpinner.selectedItem.toString()
            val selectedColor = binding.colorsSpinner.selectedItem.toString()
            addToCart(productId, selectedSize, selectedColor)
        }
    }
    private fun setupSpinner(spinner: Spinner, items: List<String>, selectedItem: String?) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        selectedItem?.let {
            val index = items.indexOf(it)
            if (index >= 0) {
                spinner.setSelection(index)
            }
        }
    }
    private fun addToCart(productId: String, selectedSize: String, selectedColor: String) {
        Log.d("ProductDetailsFragment", "Adding to cart: ProductId= $productId, Size= $selectedSize, Color= $selectedColor")
        // Logic to handle adding product to cart (e.g., calling ViewModel or CartManager)
        Toast.makeText(requireContext(), "Added to cart: $selectedSize, $selectedColor", Toast.LENGTH_SHORT).show()
    }
    private fun setupImagesRecyclerView(images: List<String>) {
        imagesAdapter = ImageAdapter(images) { selectedImage ->
            Log.d("ProductDetailsFragment", "Selected image URL: $selectedImage")
            Glide.with(this)
                .load(selectedImage)
                .into(binding.productImage)
        }
        binding.imagesRecyclerView.adapter = imagesAdapter
        binding.imagesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        // Set the first image as default
        if (images.isNotEmpty()) {
            val firstImage = images[0]
            Glide.with(this)
                .load(firstImage)
                .into(binding.productImage)
        }
    }
}
