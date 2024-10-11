package com.example.couturecorner.home.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.couturecorner.setting.viewmodel.CurrencyViewModel
import com.example.couturecorner.R
import com.example.couturecorner.brand.ui.BrandsAdapter
import com.example.couturecorner.authentication.view.LoginActivity
import com.example.couturecorner.category.ui.CategoryAdapter
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentHomeBinding
import com.example.couturecorner.home.viewmodel.HomeViewModel
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.setting.viewmodel.SettingsViewModel
import com.google.android.material.chip.Chip
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentHomeBinding
    lateinit var brandsAdapter: BrandsAdapter
    lateinit var productsAdapter: ProductsAdapter
    lateinit var categoryAdapter: CategoryAdapter
    lateinit var cuponAdapter: CuponAdapter

    @Inject
    lateinit var sharedPreference: SharedPreference
    val viewModel: HomeViewModel by viewModels()
    private val currencyViewModel: CurrencyViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    val sharedViewModel: MainViewModel by activityViewModels()

    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(8, 0, 8, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        binding.BrandsRecycle.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        // Initialize productsAdapter--------------------------------------------------------------------
        productsAdapter = ProductsAdapter(this, currencyViewModel)
        binding.productsRecycel.adapter = productsAdapter
        currencyViewModel.convertedCurrency.observe(viewLifecycleOwner) { convertedValue ->
            Log.d("HomeFragment", "Converted currency value: $convertedValue")
        }
        //------------------------------------------------------------------------------------------------
        // Initialize categoryAdapter
        categoryAdapter = CategoryAdapter {
            val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(it)
            findNavController().navigate(action)
        }
        binding.CategoryRecycel.adapter = categoryAdapter
        binding.CategoryRecycel.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        cuponAdapter = CuponAdapter()
        binding.cuponRecycle.adapter = cuponAdapter

        updateCuponsDots()

        viewModel.getCupons()

        lifecycleScope.launch {
            viewModel.cupons.collect { state ->
                when (state) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        val cupons = state.data?.data?.codeDiscountNodes?.nodes
                        cuponAdapter.submitList(cupons)
                        showLoading(false)
                    }

                    is ApiState.Error -> {
                        Log.d("AmrApollo", "${state.message} ")
                    }
                }
            }
        }
        viewModel.getFilterdProducts(null)

        // updateChips()

        sharedViewModel.getFavList()

        lifecycleScope.launch {
            sharedViewModel.favIdsList.collect {
                if (it.isNotEmpty()) {
                    productsAdapter.favListUpdate(it.toMutableList())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Fetch the filtered products and cupons again when the fragment resumes
        viewModel.getFilterdProducts(null)
        viewModel.getCupons()
        currencyViewModel.convertedCurrency.observe(viewLifecycleOwner) { convertedValue ->
            Log.d("HomeFragment", "Converted currency value: $convertedValue")
        }

        lifecycleScope.launch {
            viewModel.cupons.collect { state ->
                when (state) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        val cupons = state.data?.data?.codeDiscountNodes?.nodes
                        cuponAdapter.submitList(cupons)
                        showLoading(false)
                    }

                    is ApiState.Error -> {
                        Log.d("AmrApollo", "${state.message} ")
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.products.collect { state ->
                when (state) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        val products = state.data?.data?.products?.edges
                        showLoading(false)
                        productsAdapter.submitList(products)
                    }

                    is ApiState.Error -> {
                        Log.d("AmrApollo", "${state.message} ")
                    }
                }
            }
        }

        // Reload the favorites list to reflect any changes
        sharedViewModel.getFavList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.cuponRecycle.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    // Implement the onItemClick method from the interface
    override fun onItemClick(product: FilteredProductsQuery.Node?) {
        val action =
            HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(product?.id.toString())
        findNavController().navigate(action)
    }

    override fun deleteFavorite(productId: String) {
        sharedViewModel.removeProductFromFavorites(productId)
        Toast.makeText(requireContext(), "Deleted to favorites", Toast.LENGTH_SHORT).show()
    }


    override fun onFavoriteClick(productId: String) {
        if (isUserGuest()) {
            Log.d("FavoriteClick", "Attempted to add to favorites as a guest.")
            showLoginRequiredDialog()
        } else {
            sharedViewModel.addProductToFavorites(productId)
            Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
            Log.d("FavoriteClick", "Product with ID: $productId added to favorites.")
        }
    }

    private fun isUserGuest(): Boolean {
        val isLoggedIn = sharedPreference.isUserLoggedIn()
        Log.d("UserStatus", "User is logged in: $isLoggedIn")
        val isGuest = !isLoggedIn
        Log.d("UserStatus", "User is guest: $isGuest")
        return isGuest
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

    fun updateCuponsDots() {
        val dotImage = Array(5) { ImageView(context) }
        dotImage.forEach {
            it.setImageResource(R.drawable.dot_inactive)
            binding.dotContainer.addView(it, params)
        }

        dotImage[0].setImageResource(R.drawable.dot_active)

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dotImage.mapIndexed { index, imageView ->
                    if (position == index) {
                        imageView.setImageResource(R.drawable.dot_active)
                    } else {
                        imageView.setImageResource(R.drawable.dot_inactive)
                    }
                }
                super.onPageSelected(position)
            }
        }

        binding.cuponRecycle.registerOnPageChangeCallback(pageChangeCallback)
    }
    private fun showLoginRequiredDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_login_required, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.loginButton).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
//    fun updateChips(){
//
//        binding.chip0.isChecked = true
//        binding.chip0.setChipBackgroundColorResource(R.color.colorPrimary)
//        binding.chip0.setTextColor(Color.WHITE)
//        Log.d("AmrChips", "chip0 selected")
//        viewModel.getFilterdProducts(null)
//
//
//        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
//            // If no chip is selected (checkedId is -1), re-select chip0
////            if (checkedId == -1) {
////                binding.chip0.isChecked = true
////                binding.chip0.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
////                Log.d("AmrChips", "chip0 re-selected")
////                viewModel.getFilterdProducts(null)
////            } else {
//                // Reset all chips to their default background color
//                for (i in 0 until group.childCount) {
//                    val chip = group.getChildAt(i) as Chip
//                    chip.setChipBackgroundColorResource(R.color.white) // Default color
//                    chip.setTextColor(Color.BLACK)
//                }
//
//                // Change the background color of the selected chip
//                when (checkedId) {
//                    R.id.chip0 -> {
//                        binding.chip0.setChipBackgroundColorResource(R.color.colorPrimary)
//                        binding.chip0.setTextColor(Color.WHITE)
//                        Log.d("AmrChips", "chip0 selected")
//                        viewModel.getFilterdProducts(null)
//                    }
//                    R.id.chip1 -> {
//                        binding.chip1.setChipBackgroundColorResource(R.color.colorPrimary)
//                        binding.chip1.setTextColor(Color.WHITE)
//                        Log.d("AmrChips", "chip1 selected")
//                        viewModel.getFilterdProducts("product_type:${binding.chip1.text}")
//                    }
//                    R.id.chip2 -> {
//                        binding.chip2.setChipBackgroundColorResource(R.color.colorPrimary)
//                        binding.chip2.setTextColor(Color.WHITE)
//                        Log.d("AmrChips", "chip2 selected")
//                        viewModel.getFilterdProducts("product_type:${binding.chip2.text}")
//                    }
//                    R.id.chip3 -> {
//                        binding.chip3.setChipBackgroundColorResource(R.color.colorPrimary)
//                        binding.chip3.setTextColor(Color.WHITE)
//                        Log.d("AmrChips", "chip3 selected")
//                        viewModel.getFilterdProducts("product_type:${binding.chip3.text}")
//                    }
//                }
// //           }
//        }
//    }



