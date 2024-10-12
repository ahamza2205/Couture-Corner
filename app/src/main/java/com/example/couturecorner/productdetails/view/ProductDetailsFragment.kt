package com.example.couturecorner.productdetails.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.example.couturecorner.data.model.CartItem
import com.example.couturecorner.databinding.FragmentProductDetailsBinding
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.productdetails.viewmodel.ProductDetailsViewModel
import com.example.couturecorner.setting.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: ProductDetailsViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    private var title: String? = null
    private var imagUrl: String? = null
    private var price : String? = null
    private  var stockQuantity: String? = null

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
        cartViewModel.getCartItems()  // Trigger fetching cart items

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
                        Log.d("ProductDetailsFragment", "Loading product details...")
                    }

                    is ApiState.Success -> {
                        val product = state.data?.product
                        Log.d("ProductDetailsFragment", "Product details received: $product")
                        product?.let {
                            title=  it.title
                            binding.productTitle.text =title

                            val colorFromTitle = it.title.split("|").lastOrNull()?.trim()
                            val variants = it.variants?.edges?.mapNotNull { variant ->
                                variant?.node
                            } ?: listOf()
                            // Extract available sizes and colors
                            val availableSizes = variants.map { variant ->
                                variant.selectedOptions?.find { option -> option?.name == "Size" }?.value ?: ""
                            }
                            val availableColors = variants.map { variant ->
                                variant.selectedOptions?.find { option -> option?.name == "Color" }?.value ?: ""
                            }
                            // Update UI components
                            binding.productDescription.text = it.description
                            price=it.variants?.edges?.get(0)?.node?.price
                            binding.priceValue.text = "${price}"
                            binding.productTypeValue.text = it.productType
                            stockQuantity = it.totalInventory.toString()
                            binding.stockCount.text = "${stockQuantity} items available"
                            imagUrl = it.images?.edges?.firstOrNull()?.node?.src
                            it.images?.edges?.let { imageEdges ->
                                setupImagesRecyclerView(imageEdges.map { imageEdge -> imageEdge?.node?.src ?: "" })
                            }

                            // Set up  spinners
                            setupSpinner(binding.sizesSpinner, availableSizes.distinct(), null)
                            setupSpinner(binding.colorsSpinner, availableColors.distinct(), colorFromTitle)

                            binding.btnAddToCart.setOnClickListener {
                                val selectedSize = binding.sizesSpinner.selectedItem.toString()
                                val selectedColor = binding.colorsSpinner.selectedItem.toString()
                                // Find the matching variant by size and color
                                val selectedVariant = variants.find { variant ->
                                    val size = variant.selectedOptions?.find { it?.name == "Size" }?.value
                                    val color = variant.selectedOptions?.find { it?.name == "Color" }?.value
                                    size == selectedSize && color == selectedColor
                                }
                                if (selectedVariant != null) {
                                    addToCart(selectedVariant.id, selectedSize, selectedColor)
                                } else {
                                    Toast.makeText(requireContext(), "Variant not found for selected size and color", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    is ApiState.Error -> {
                        Log.e("ProductDetailsFragment", "Error fetching product details: ${state.message}")
                    }
                }
            }
        }
    }
    private fun addToCart(variantId: String, selectedSize: String, selectedColor: String) {
        val newItem = CartItem(
            id = variantId,
            quantity = 1,
            imageUrl = imagUrl,
            name = title,
            price = price,
            color = selectedColor,
            size =selectedSize,
            inventoryQuantity = stockQuantity?.toInt(),
        )
        cartViewModel.addedToCart(newItem)

        Log.d("ProductDetailsFragment", "Adding to cart: VariantId= $variantId, Size= $selectedSize, Color= $selectedColor")
        Toast.makeText(requireContext(), "Added to cart: Size= $selectedSize, Color= $selectedColor", Toast.LENGTH_SHORT).show()
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
