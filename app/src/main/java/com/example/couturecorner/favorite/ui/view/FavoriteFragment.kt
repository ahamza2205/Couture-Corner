package com.example.couturecorner.favorite.ui.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.authentication.view.LoginActivity
import com.example.couturecorner.setting.viewmodel.CurrencyViewModel
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
    private val currencyViewModel: CurrencyViewModel by viewModels()
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
                    binding.progressBar2.visibility = View.VISIBLE
                    binding.recyclerViewFavorites.visibility = View.GONE
                }
                is ApiState.Success<List<ProductQuery.Product>> -> {
                    val products = state.data ?: emptyList()
                    binding.progressBar2.visibility = View.GONE

                    if (products.isEmpty()) {
                        binding.recyclerViewFavorites.visibility = View.GONE
                        binding.imageView3.visibility = View.VISIBLE
                        binding.textView4.visibility = View.VISIBLE
                    } else {
                        binding.imageView3.visibility = View.GONE
                        binding.textView4.visibility = View.GONE

                        binding.recyclerViewFavorites.apply {
                            visibility = View.VISIBLE
                            alpha = 0f
                            animate()
                                .alpha(1f)
                                .setDuration(1000)
                                .start()
                        }
                        productsAdapter.submitList(products)
                    }
                }
                is ApiState.Error -> {
                    binding.progressBar2.visibility = View.GONE
                    binding.recyclerViewFavorites.visibility = View.GONE
                    binding.imageView3.visibility = View.VISIBLE
                    binding.textView4.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        favoriteViewModel.loadFavoriteProducts(customerId)
    }


    private fun setupRecyclerView() {
        productsAdapter = FavoriteProductsAdapter(this , currencyViewModel)
        binding.recyclerViewFavorites.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = productsAdapter
        }
    }
    override fun onItemClick(product: ProductQuery.Product) {
        val action = FavoriteFragmentDirections.actionFavoriteFragmentToProductDetailsFragment(product?.id.toString())
        findNavController().navigate(action)    }
    override fun onFavoriteClick(productId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                val customerId = sharedPreference.getShopifyUserId(userEmail)
                if (customerId != null) {
                    favoriteViewModel.removeProductFromFavorites(customerId, productId)
                } else {
                    Toast.makeText(requireContext(), "Failed to get customer ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Failed to get user email", Toast.LENGTH_SHORT).show()
            }
        } else {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showLoginRequiredDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_login_required, null)
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
