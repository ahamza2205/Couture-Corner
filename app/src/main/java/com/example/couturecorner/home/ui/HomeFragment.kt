package com.example.couturecorner.home.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {

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

        brandsAdapter= BrandsAdapter{  val action = HomeFragmentDirections.actionHomeFragmentToBrandsFragment(it)
            findNavController().navigate(action)}


        binding.BrandsRecycle.adapter=brandsAdapter
        binding.BrandsRecycle.layoutManager= LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)

        productsAdapter=ProductsAdapter()
        binding.productsRecycel.adapter=productsAdapter

        categoryAdapter= CategoryAdapter{val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(it)
            findNavController().navigate(action)}

        binding.CategoryRecycel.adapter=categoryAdapter
        binding.CategoryRecycel.layoutManager=LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)

       // viewModel.getFilterdProducts(null)

        updateChips()

//
//        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
//            // Reset all chips to their default background color
//            for (i in 0 until group.childCount) {
//                val chip = group.getChildAt(i) as Chip
//                chip.setChipBackgroundColorResource(R.color.white) // Default color
//            }
//
//            // Change the background color of the selected chip
//            when (checkedId) {
//                R.id.chip0 -> {
//                   binding.chip0.setChipBackgroundColorResource(R.color.colorPrimary)
//                    Log.d("AmrChips", "chip0 selected")
//                    viewModel.getFilterdProducts(null)
//                }
//                R.id.chip1 -> {
//                    binding.chip1.setChipBackgroundColorResource(R.color.colorPrimary)
//                    Log.d("AmrChips", "chip1 selected")
//                    viewModel.getFilterdProducts("product_type:${binding.chip1.text}")
//                }
//                R.id.chip2 -> {
//                    binding.chip2.setChipBackgroundColorResource(R.color.colorPrimary)
//                    Log.d("AmrChips", "chip2 selected")
//                    viewModel.getFilterdProducts("product_type:${binding.chip2.text}")
//                }
//                R.id.chip3 -> {
//                    binding.chip3.setChipBackgroundColorResource(R.color.colorPrimary)
//                    Log.d("AmrChips", "chip3 selected")
//                    viewModel.getFilterdProducts("product_type:${binding.chip3.text}")
//                }
//            }
//        }



        lifecycleScope.launch {
            viewModel.products.collect{
                when(it){
                    is ApiState.Loading->showLoading(true)
                    is ApiState.Success->{
                        val products = it.data.data?.products?.edges
                        productsAdapter.submitList(products)
                        showLoading(false)
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

//    fun updateChips() {
//        // Initially set chip0 to be checked
//        binding.chip0.isChecked = true
//        binding.chip0.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
//        Log.d("AmrChips", "chip0 selected")
//        viewModel.getFilterdProducts(null)
//
//        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
//            // Reset all chips to their default background color
//            for (i in 0 until group.childCount) {
//                val chip = group.getChildAt(i) as Chip
//                chip.setChipBackgroundColorResource(R.color.white) // Default color
//            }
//
//            // Change the background color of the selected chip
//            when (checkedId) {
//                R.id.chip0 -> {
//                    binding.chip0.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
//                    Log.d("AmrChips", "chip0 selected")
//                    viewModel.getFilterdProducts(null)
//                }
//                R.id.chip1 -> {
//                    binding.chip1.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
//                    Log.d("AmrChips", "chip1 selected")
//                    viewModel.getFilterdProducts("product_type:${binding.chip1.text}")
//                }
//                R.id.chip2 -> {
//                    binding.chip2.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
//                    Log.d("AmrChips", "chip2 selected")
//                    viewModel.getFilterdProducts("product_type:${binding.chip2.text}")
//                }
//                R.id.chip3 -> {
//                    binding.chip3.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
//                    Log.d("AmrChips", "chip3 selected")
//                    viewModel.getFilterdProducts("product_type:${binding.chip3.text}")
//                }
//            }
//
//            // If no chip is selected, re-select chip0
//            if (checkedId == -1) {
//                binding.chip0.isChecked = true
//                binding.chip0.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
//                Log.d("AmrChips", "chip0 re-selected")
//                viewModel.getFilterdProducts(null)
//            }
//        }
//    }



}

