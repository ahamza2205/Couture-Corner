package com.example.couturecorner.favorite.ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.favorite.ui.viewmodel.FavoriteViewModel
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentFavoriteBinding
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.graphql.ProductQuery
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteFragment : Fragment(), OnFavoriteItemClickListener {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var sharedPreference: SharedPreference

    private val favoriteViewModel: FavoriteViewModel by viewModels()
    val sharedViewModel: MainViewModel by activityViewModels()

    private lateinit var productsAdapter: FavoriteProductsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadFavoriteProducts()

    }
    private fun loadFavoriteProducts() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                val customerId = sharedPreference.getShopifyUserId(userEmail)
                if (customerId != null) {
                    observeFavoriteProducts(customerId)
                } else {
                    Toast.makeText(requireContext(), "Failed to get customer ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Failed to get user email", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
    private fun observeFavoriteProducts(customerId: String) {
        favoriteViewModel.favoriteProducts.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ApiState.Loading -> {
                }
                is ApiState.Success<List<ProductQuery.Product>> -> {
                    val products = state.data ?: emptyList()
                    productsAdapter.submitList(products)
                }
                is ApiState.Error -> {
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        favoriteViewModel.loadFavoriteProducts(customerId)
    }
    private fun setupRecyclerView() {
        productsAdapter = FavoriteProductsAdapter(this)
        binding.recyclerViewFavorites.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productsAdapter
        }
    }
    override fun onItemClick(product: ProductQuery.Product) {
        Toast.makeText(requireContext(), "Clicked on: ${product.title}", Toast.LENGTH_SHORT).show()
    }
    override fun onFavoriteClick(productId: String) {
        Toast.makeText(requireContext(), "Favorite clicked for: $productId", Toast.LENGTH_SHORT).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
