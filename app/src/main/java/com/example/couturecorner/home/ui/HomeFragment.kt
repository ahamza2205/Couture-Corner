package com.example.couturecorner.home.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.R
import com.example.couturecorner.category.ui.CategoryAdapter
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentHomeBinding
import com.example.couturecorner.home.viewmodel.HomeViewModel
import com.google.android.material.chip.Chip
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentHomeBinding
    lateinit var brandsAdapter: BrandsAdapter
    lateinit var productsAdapter: ProductsAdapter
    lateinit var categoryAdapter: CategoryAdapter

    val viewModel: HomeViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapters first
        brandsAdapter = BrandsAdapter {
            val action = HomeFragmentDirections.actionHomeFragmentToBrandsFragment(it)
            findNavController().navigate(action)
        }
        binding.BrandsRecycle.adapter = brandsAdapter
        binding.BrandsRecycle.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        // Initialize productsAdapter
        productsAdapter = ProductsAdapter(this)
        binding.productsRecycel.adapter = productsAdapter

        // Initialize categoryAdapter
        categoryAdapter = CategoryAdapter {
            val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(it)
            findNavController().navigate(action)
        }
        binding.CategoryRecycel.adapter = categoryAdapter
        binding.CategoryRecycel.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

       // viewModel.getFilterdProducts(null)

        updateChips()


        lifecycleScope.launch {
            viewModel.products.collect { state ->
                when (state) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        val products = state.data?.data?.products?.edges
                        productsAdapter.submitList(products) // Use the initialized adapter here
                        showLoading(false)
                        products?.forEach { edge ->
                            val product = edge?.node
                            Log.d("AmrApollo", "Product: ${product?.title}, Description: ")
                        }
                    }
                    is ApiState.Error -> {
                        Log.d("AmrApollo", "${state.message} ")
                    }
                }
            }
        }
    }

    // Implement the onItemClick method from the interface
    override fun onItemClick(product: FilteredProductsQuery.Node?) {
        val action = HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(product?.id.toString())
        findNavController().navigate(action)
    }
    override fun onFavoriteClick(productId: String) {
        viewModel.addProductToFavorites(productId)
        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
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
        viewModel.getFilterdProducts(null)


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
                        viewModel.getFilterdProducts(null)
                    }
                    R.id.chip1 -> {
                        binding.chip1.setChipBackgroundColorResource(R.color.colorPrimary)
                        binding.chip1.setTextColor(Color.WHITE)
                        Log.d("AmrChips", "chip1 selected")
                        viewModel.getFilterdProducts("product_type:${binding.chip1.text}")
                    }
                    R.id.chip2 -> {
                        binding.chip2.setChipBackgroundColorResource(R.color.colorPrimary)
                        binding.chip2.setTextColor(Color.WHITE)
                        Log.d("AmrChips", "chip2 selected")
                        viewModel.getFilterdProducts("product_type:${binding.chip2.text}")
                    }
                    R.id.chip3 -> {
                        binding.chip3.setChipBackgroundColorResource(R.color.colorPrimary)
                        binding.chip3.setTextColor(Color.WHITE)
                        Log.d("AmrChips", "chip3 selected")
                        viewModel.getFilterdProducts("product_type:${binding.chip3.text}")
                    }
                }
 //           }
        }
    }


}

