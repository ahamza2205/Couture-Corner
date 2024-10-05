package com.example.couturecorner.home.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.home.ProductAdapter
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        NavigationUI.setupActionBarWithNavController(this, navController)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        productAdapter = ProductAdapter(emptyList()) { productId ->
            // Store the selected product ID in the ViewModel
            viewModel.selectedProductId = productId
            // Navigate to ProductDetailsFragment directly
            navController.navigate(R.id.productDetailsFragment)
        }
        recyclerView.adapter = productAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Hide the RecyclerView initially
        recyclerView.visibility = View.GONE

        viewModel.getProducts()

        lifecycleScope.launch {
            viewModel.productsApollo.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                        // Handle loading state if needed
                    }
                    is ApiState.Success -> {
                        val products = apiState.data.data?.products?.edges
                        productAdapter = ProductAdapter(products ?: emptyList()) { productId ->
                            viewModel.selectedProductId = productId
                            navController.navigate(R.id.productDetailsFragment)
                        }
                        recyclerView.adapter = productAdapter
                        // Keep the RecyclerView hidden until search is performed
                    }
                    is ApiState.Error -> {
                        Log.d("MainActivity", "${apiState.message}")
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.queryHint = "Search for products..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    recyclerView.visibility = View.VISIBLE // Show when there's text
                    productAdapter.filter(newText)
                } else {
                    recyclerView.visibility = View.GONE // Hide when no text
                }
                return true
            }
        })

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
