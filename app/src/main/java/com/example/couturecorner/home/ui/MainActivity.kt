package com.example.couturecorner.home.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.example.couturecorner.Utility.NetworkUtils
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var networkUtils: NetworkUtils
    private lateinit var navController: androidx.navigation.NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomnavigation)
        networkUtils = NetworkUtils(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        observeNetworkState() // Check for network connection before proceeding

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = if (destination.id == R.id.productDetailsFragment) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        bottomNav.setupWithNavController(navController)

        NavigationUI.setupActionBarWithNavController(this, navController)

        recyclerView = findViewById(R.id.recyclerView)
        productAdapter = ProductAdapter(emptyList()) { productId ->
            viewModel.selectedProductId = productId
            navController.navigate(R.id.productDetailsFragment)
        }
        recyclerView.adapter = productAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.visibility = View.GONE

        // Load products after checking network connection
        if (networkUtils.hasNetworkConnection()) {
            viewModel.getProducts()
            observeProductState()
        }
    }

    private fun observeProductState() {
        lifecycleScope.launch {
            viewModel.productsApollo.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                        // Handle loading state if needed
                    }
                    is ApiState.Success -> {
                        val products = apiState.data?.data?.products?.edges
                        productAdapter = ProductAdapter(products ?: emptyList()) { productId ->
                            viewModel.selectedProductId = productId
                            navController.navigate(R.id.productDetailsFragment)
                        }
                        recyclerView.adapter = productAdapter
                    }
                    is ApiState.Error -> {
                        Log.d("MainActivity", "${apiState.message}")
                    }
                }
            }
        }
    }

    private fun observeNetworkState() {
        lifecycleScope.launch {
            networkUtils.observeNetworkState().collect { isConnected ->
                val fragmentContainer = findViewById<View>(R.id.nav_host_fragment)
                val toolbar = findViewById<View>(R.id.toolbar) // Get the toolbar reference
                 val bottomnavigation = findViewById<View>(R.id.bottomnavigation)
                if (!isConnected) {
                    fragmentContainer.visibility = View.GONE
                    toolbar.visibility = View.GONE
                    bottomnavigation.visibility = View.GONE

                    // Show dialog and keep it until the network is reconnected
                    Dialog.showCustomDialog(
                        context = this@MainActivity,
                        message = "No internet connection. Please try again.",
                        positiveButtonText = "Ok",
                        negativeButtonText = "Exit",
                        lottieAnimationResId = R.raw.network_error_animation,
                        positiveAction = {
                            if (networkUtils.hasNetworkConnection()) {
                                fragmentContainer.visibility = View.VISIBLE
                                toolbar.visibility = View.VISIBLE
                                bottomnavigation.visibility = View.VISIBLE
                                observeProductState()
                            } else {
                                observeNetworkState()
                            }
                        },
                        negativeAction = {
                            finishAffinity()
                        }
                    )
                } else {
                    fragmentContainer.visibility = View.VISIBLE
                    toolbar.visibility = View.VISIBLE
                    bottomnavigation.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView
        searchView.queryHint = "Search for products..."
        // Set up SearchView listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    productAdapter.filter(newText)
                } else {
                    recyclerView.visibility = View.GONE
                }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
   /*         R.id.action_cart -> {
                // Collapse the SearchView if it's open
                if (!searchView.isIconified) {
                    searchView.setIconified(true)
                }
                // Navigate to CartFragment when the cart item is clicked
                navController.navigate(R.id.cartFragment)
                true
            }*/
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun hideBottomNav() {
        bottomNav.visibility = View.GONE
    }

    fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }
}
