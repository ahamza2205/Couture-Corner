package com.example.couturecorner.brand.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.couturecorner.setting.viewmodel.CurrencyViewModel
import com.example.couturecorner.R
import com.example.couturecorner.brand.viewModel.BrandViewModel
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentBrandsBinding
import com.example.couturecorner.home.ui.OnItemClickListener
import com.example.couturecorner.home.ui.ProductsAdapter
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.google.android.material.chip.Chip
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BrandsFragment : Fragment(), OnItemClickListener {

    private var brandName: String? = null

    val viewModel:BrandViewModel by viewModels()

    val sharedViewModel: MainViewModel by activityViewModels()

    lateinit var binding:FragmentBrandsBinding
    lateinit var productsBrandAdapter: ProductsAdapter


    val brandLogos: Map<String, Int> = mapOf("vans" to R.drawable.vans, "palladium" to R.drawable.palladium_logo,
        "asics tiger" to R.drawable.asics_tiger_logo, "puma" to R.drawable.puma, "supra" to R.drawable.supra_logo,
        "adidas" to R.drawable.adidas, "timberland" to R.drawable.timberland_logo, "nike" to R.drawable.nike,
        "dr martens" to R.drawable.dr_martins_logo, "converse" to R.drawable.converse_logo,
        "herschel" to R.drawable.herschel_logo ,"flex fit" to R.drawable.flexfit_logo)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        brandName = arguments?.getString("brand")
        binding = FragmentBrandsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pass 'this' as the listener
        productsBrandAdapter = ProductsAdapter(this)
        binding.productsRecycel.adapter = productsBrandAdapter

        val logoResId = brandLogos[brandName] ?: R.drawable.shoz10
        binding.AddsImageView.setImageResource(logoResId)

//        viewModel.getFilterdProducts(brandName)
        updateChips()

        lifecycleScope.launch {
            viewModel.productsBrands.collect {
                when (it) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        val products = it.data?.data?.products?.edges
                        prepareProductsForAdapter(products ?: emptyList())
//                        productsBrandAdapter.submitList(products)
                        showLoading(false)
                    }
                    is ApiState.Error -> {
                        showLoading(false)
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }

        sharedViewModel.getFavList()

        lifecycleScope.launch {
            sharedViewModel.favIdsList.collect{
                if(it.isNotEmpty()){
                    productsBrandAdapter.favListUpdate(it.toMutableList())
                }
            }
        }

    }
    override fun onItemClick(product: FilteredProductsQuery.Node?) {
        val action = BrandsFragmentDirections.actionBrandsFragmentToProductDetailsFragment(product?.id.toString())
        findNavController().navigate(action)
    }
    override fun onFavoriteClick(productId: String) {
        sharedViewModel.addProductToFavorites(productId)
        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
        Log.d("BrandFragment", "Favorited product ID: $productId")
    }

    override fun deleteFavorite(productId: String) {
        sharedViewModel.removeProductFromFavorites(productId)
        Toast.makeText(requireContext(), "Deleted to favorites", Toast.LENGTH_SHORT).show()
    }

    override fun getcurrency(): String {
        return getCurrencySymbol(sharedViewModel.getSelectedCurrency()?: "EGP")
    }

    fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.productsRecycel.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.productsRecycel.visibility = View.VISIBLE
        }
    }


    fun updateChips(){

        binding.chip0.isChecked = true
        binding.chip0.setChipBackgroundColorResource(R.color.colorPrimary)
        binding.chip0.setTextColor(Color.WHITE)
        Log.d("AmrChips", "chip0 selected")
        viewModel.getFilterdProducts(brandName)


        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            // If no chip is selected (checkedId is -1), re-select chip0
//            if (checkedId == -1) {
//                binding.chip0.isChecked = true
//                binding.chip0.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
//                Log.d("AmrChips", "chip0 re-selected")
//                viewModel.getFilterdProducts(null)
//            } else {
            // Reset all chips to their default background color
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.setChipBackgroundColorResource(R.color.white) // Default color
                chip.setTextColor(Color.BLACK)
            }

            // Change the background color of the selected chip
            when (checkedId) {
                R.id.chip0 -> {
                    binding.chip0.setChipBackgroundColorResource(R.color.colorPrimary)
                    binding.chip0.setTextColor(Color.WHITE)
                    Log.d("AmrChips", "chip0 selected")
                    viewModel.getFilterdProducts(brandName)
                }
                R.id.chip1 -> {
                    binding.chip1.setChipBackgroundColorResource(R.color.colorPrimary)
                    binding.chip1.setTextColor(Color.WHITE)
                    Log.d("AmrChips", "chip1 selected")
                    viewModel.getFilterdProducts("product_type:${binding.chip1.text} AND $brandName")
                }
                R.id.chip2 -> {
                    binding.chip2.setChipBackgroundColorResource(R.color.colorPrimary)
                    binding.chip2.setTextColor(Color.WHITE)
                    Log.d("AmrChips", "chip2 selected")
                    viewModel.getFilterdProducts("product_type:${binding.chip2.text} AND $brandName")
                }
                R.id.chip3 -> {
                    binding.chip3.setChipBackgroundColorResource(R.color.colorPrimary)
                    binding.chip3.setTextColor(Color.WHITE)
                    Log.d("AmrChips", "chip3 selected")
                    viewModel.getFilterdProducts("product_type:${binding.chip3.text} AND $brandName")
                }
            }
            //           }
        }
    }

    private fun prepareProductsForAdapter(products: List<FilteredProductsQuery.Edge?>) {
      if (products.isNotEmpty()) {
          val updatedProducts = products.map { product ->
              val productId = product?.node?.id
              val originalPrice = product?.node?.variants?.edges?.get(0)?.node?.price?.toDoubleOrNull() ?: 0.0

              // Trigger conversion for each product
              sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", originalPrice, productId ?: "")

              // Return the original product, the price will be updated later
              product
          }

          // Observe currency conversion updates
          lifecycleScope.launch {
              sharedViewModel.convertedCurrency.collect { conversions ->
                  val updatedList = updatedProducts.map { product ->
                      val productId = product?.node?.id
                      val convertedPrice = conversions[productId] ?: product?.node?.variants?.edges?.get(0)?.node?.price?.toDoubleOrNull() ?: 0.0

                      val price = product?.node?.variants?.edges?.get(0)?.node?.price
                      // Create a copy or modify the product item with the new price
                      product?.copy(
                          node = product.node?.copy(
                              variants = product.node.variants?.copy(
                                  edges = product.node.variants.edges?.map { edge ->
                                      edge?.copy(node = edge.node?.copy(price =  convertedPrice.toString()))
                                  }
                              )
                          )
                      )
                  }
                  Log.d("CheckForConversion", "${updatedList[0]?.node?.variants?.edges?.get(0)?.node?.price}: ")

                  // Submit the updated list with converted prices to the adapter
                  showLoading(false)
                  productsBrandAdapter.submitList(updatedList)
              }
          }
      }
        else
        {
            productsBrandAdapter.submitList(products)
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

//    override fun isFavorite(productId: String): Boolean {
//        TODO("Not yet implemented")
//    }
}