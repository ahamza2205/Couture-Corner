package com.example.couturecorner.home.ui

import android.app.AlertDialog
import android.content.Intent
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
import com.example.couturecorner.data.local.SharedPreferenceImp
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentHomeBinding
import com.example.couturecorner.home.viewmodel.HomeViewModel
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.setting.viewmodel.SettingsViewModel
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
    lateinit var sharedPreference: SharedPreferenceImp
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
        productsAdapter = ProductsAdapter(this)
        binding.productsRecycel.adapter = productsAdapter
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
                    currencyViewModel.convertedCurrency.observe(viewLifecycleOwner) { convertedValue ->
                        Log.d("HomeFragment", "Converted currency value: $convertedValue")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Fetch the filtered products and cupons again when the fragment resumes
        viewModel.getFilterdProducts(null)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collect { state ->
                when (state) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        val products = state.data?.data?.products?.edges
                        showLoading(true)
                        prepareProductsForAdapter(products ?: emptyList())

                        //productsAdapter.submitList(products)
//                        currencyViewModel.convertedCurrency.observe(viewLifecycleOwner) { convertedValue ->
//                            Log.d("HomeFragment", "Converted currency value: $convertedValue")
//                        }
                    }

                    is ApiState.Error -> {
                        Log.d("AmrApollo", "${state.message} ")
                    }
                }
            }
        }
        lifecycleScope.launch {
            sharedViewModel.favIdsList.collect {
                if (it.isNotEmpty()) {
                    productsAdapter.favListUpdate(it.toMutableList())
                    currencyViewModel.convertedCurrency.observe(viewLifecycleOwner) { convertedValue ->
                        Log.d("HomeFragment", "Converted currency value: $convertedValue")
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
              productsAdapter.submitList(updatedList)
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
}
