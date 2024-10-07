package com.example.couturecorner.brand.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.couturecorner.R
import com.example.couturecorner.brand.viewModel.BrandViewModel
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentBrandsBinding
import com.example.couturecorner.home.ui.ProductsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BrandsFragment : Fragment() {

    private var brandName: String? = null

    val viewModel:BrandViewModel by viewModels()

    lateinit var binding:FragmentBrandsBinding
    lateinit var productsBrandAdapter: ProductsAdapter


    val brandLogos: Map<String, Int> = mapOf("vans" to R.drawable.vans_logo, "palladium" to R.drawable.palladium_logo,
        "asics tiger" to R.drawable.asics_tiger_logo, "puma" to R.drawable.puma_logo, "supra" to R.drawable.supra_logo,
        "adidas" to R.drawable.adidas_logo, "timberland" to R.drawable.timberland_logo, "nike" to R.drawable.nike,
        "dr martens" to R.drawable.dr_martins_logo, "converse" to R.drawable.converse_logo,
        "herschel" to R.drawable.herschel_logo ,"flex fit" to R.drawable.flexfit_logo)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        brandName = arguments?.getString("brand")
        Log.d("BrandArgsTest", "$brandName: ")

        binding= FragmentBrandsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productsBrandAdapter=ProductsAdapter()
        binding.productsRecycel.adapter=productsBrandAdapter


        val logoResId = brandLogos[brandName] ?: R.drawable.shoz10
        binding.AddsImageView.setImageResource(logoResId)


        viewModel.getFilterdProducts(brandName ?: "puma")

        lifecycleScope.launch {
            viewModel.productsBrands.collect {
                when (it) {
                    is ApiState.Loading -> {showLoading(true)}
                    is ApiState.Success -> {
                        val products = it.data.data?.products?.edges
                        productsBrandAdapter.submitList(products)
                        showLoading(false)
                        products?.forEach { edge ->
                            val product = edge?.node
                            Log.d("AmrBrandsApollo", "Product: ${product?.title}, Description: ")
                        }
                    }

                    is ApiState.Error -> {
                        showLoading(false)
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }
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