package com.example.couturecorner.category.ui

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
import com.example.couturecorner.category.viewModel.CategoryViewModel
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentCategoryBinding
import com.example.couturecorner.home.ui.OnItemClickListener
import com.example.couturecorner.home.ui.ProductsAdapter
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.google.android.material.chip.Chip
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryFragment : Fragment(), OnItemClickListener {

    private var category: String? = null
    lateinit var binding:FragmentCategoryBinding
    val viewModel: CategoryViewModel by viewModels()
    val sharedViewModel: MainViewModel by activityViewModels()

    lateinit var categoryAdapter:ProductsAdapter

    val categoryLogos: Map<String, Int> = mapOf("women" to R.drawable.woman, "men" to R.drawable.men,
        "kid" to R.drawable.shopping, "sale" to R.drawable.sale)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        category = arguments?.getString("category")
        Log.d("CategoryArgsTest", "$category: ")
       binding= FragmentCategoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        categoryAdapter = ProductsAdapter(this)
        binding.productsRecycel.adapter=categoryAdapter

       // viewModel.getFilterdProducts(category)
        updateChips()

        val logoResId = categoryLogos[category] ?: R.drawable.shoz10
        binding.categoryImageView.setImageResource(logoResId)

        lifecycleScope.launch {
            viewModel.productsCategory.collect{
                when(it){
                    is ApiState.Loading->showLoading(true)
                    is ApiState.Success->{
                        val products = it.data?.data?.products?.edges
                        showLoading(false)
                        prepareProductsForAdapter(products ?: emptyList())
//                        categoryAdapter.submitList(products)
//                        products?.forEach { edge ->
//                            val productTag = edge?.node?.tags
//                           // productTag?.forEach { tag -> Log.d("AmrCategoryApollo", "tag: ${tag}, Description: ") }
//                            Log.d("AmrCategoryApollo", "Product: ${edge?.node?.title}, Description: ")
//                        }
                    }
                    is ApiState.Error->{
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
                    categoryAdapter.favListUpdate(it.toMutableList())
                }
            }
        }

    }
    override fun onItemClick(product: FilteredProductsQuery.Node?) {
        // Handle item click, e.g., navigate to a detailed product page
        val action = CategoryFragmentDirections.actionCategoryFragmentToProductDetailsFragment(product?.id.toString())
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

    fun showLoading(isLoading:Boolean)
    {
        if (isLoading)
        {
            binding.progressBar.visibility=View.VISIBLE
            binding.productsRecycel.visibility=View.GONE
        }
        else
        {
            binding.progressBar.visibility=View.GONE
            binding.productsRecycel.visibility=View.VISIBLE
        }
    }


    fun updateChips(){

        binding.chip0.isChecked = true
        binding.chip0.setChipBackgroundColorResource(R.color.colorPrimary)
        binding.chip0.setTextColor(Color.WHITE)
        Log.d("AmrChips", "chip0 selected")
        viewModel.getFilterdProducts(category)


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
                    viewModel.getFilterdProducts(category)
                }
                R.id.chip1 -> {
                    binding.chip1.setChipBackgroundColorResource(R.color.colorPrimary)
                    binding.chip1.setTextColor(Color.WHITE)
                    Log.d("AmrChips", "chip1 selected")
                    viewModel.getFilterdProducts("product_type:${binding.chip1.text} AND $category")
                }
                R.id.chip2 -> {
                    binding.chip2.setChipBackgroundColorResource(R.color.colorPrimary)
                    binding.chip2.setTextColor(Color.WHITE)
                    Log.d("AmrChips", "chip2 selected")
                    viewModel.getFilterdProducts("product_type:${binding.chip2.text} AND $category")
                }
                R.id.chip3 -> {
                    binding.chip3.setChipBackgroundColorResource(R.color.colorPrimary)
                    binding.chip3.setTextColor(Color.WHITE)
                    Log.d("AmrChips", "chip3 selected")
                    viewModel.getFilterdProducts("product_type:${binding.chip3.text} AND $category")
                }
            }
            //           }
        }
    }

    private fun prepareProductsForAdapter(products: List<FilteredProductsQuery.Edge?>) {
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
                                    edge?.copy(node = edge.node?.copy(price = getString(
                                        R.string.price,
                                        convertedPrice.toString(),
                                        getCurrencySymbol(sharedViewModel.getSelectedCurrency() ?: "EGP")
                                    )))
                                }
                            )
                        )
                    )
                }
                Log.d("CheckForConversion", "${updatedList[0]?.node?.variants?.edges?.get(0)?.node?.price}: ")

                // Submit the updated list with converted prices to the adapter
                showLoading(false)
                categoryAdapter.submitList(updatedList)
            }
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