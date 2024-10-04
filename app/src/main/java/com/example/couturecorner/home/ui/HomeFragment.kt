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
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentHomeBinding
import com.example.couturecorner.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var brandsAdapter: BrandsAdapter
    lateinit var productsAdapter: ProductsAdapter

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

        brandsAdapter= BrandsAdapter{  val action = HomeFragmentDirections.actionHomeFragmentToBrandsFragment(it)
            findNavController().navigate(action)}


        binding.BrandsRecycle.adapter=brandsAdapter
        binding.BrandsRecycle.layoutManager= LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)

        productsAdapter=ProductsAdapter()
        binding.productsRecycel.adapter=productsAdapter

        viewModel.getProducts()

        lifecycleScope.launch {
            viewModel.products.collect{
                when(it){
                    is ApiState.Loading->{}
                    is ApiState.Success->{
                        val products = it.data.data?.products?.edges
                        productsAdapter.submitList(products)
                        products?.forEach { edge ->
                            val product = edge?.node
                            Log.d("AmrApollo", "Product: ${product?.title}, Description: ")
                        }
                    }
                    is ApiState.Error->{
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }

    }


}