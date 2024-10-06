package com.example.couturecorner.home.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.category.ui.CategoryAdapter
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentHomeBinding
import com.example.couturecorner.home.viewmodel.HomeViewModel
import com.graphql.HomeProductsQuery
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
        productsAdapter = ProductsAdapter(this) // Pass this instance
        binding.productsRecycel.adapter = productsAdapter

        // Initialize categoryAdapter
        categoryAdapter = CategoryAdapter {
            val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(it)
            findNavController().navigate(action)
        }
        binding.CategoryRecycel.adapter = categoryAdapter
        binding.CategoryRecycel.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        viewModel.getProducts()

        lifecycleScope.launch {
            viewModel.products.collect { state ->
                when (state) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        val products = state.data.data?.products?.edges
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
    override fun onItemClick(product: HomeProductsQuery.Node?) {
        val action = HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(product?.id.toString())
        findNavController().navigate(action)
    }

    // Handle favorite click
    override fun onFavoriteClick(productId: String) {
        Log.d("HomeFragment", "Favorite clicked for product ID: $productId")
        // Call the ViewModel to add product to favorites
        viewModel.addProductToFavorites(productId)
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


}

