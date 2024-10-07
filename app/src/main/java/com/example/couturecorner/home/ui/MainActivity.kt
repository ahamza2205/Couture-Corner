package com.example.couturecorner.home.ui
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val viewModel: MainViewModel by viewModels()
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNav = findViewById(R.id.bottom_navigation)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNav.setupWithNavController(navController)

        NavigationUI.setupActionBarWithNavController(this, navController)

        viewModel.getProducts()
        lifecycleScope.launch {
            viewModel.productsApollo.collect {
                when (it) {
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        val products = it.data.data?.products?.edges
                        products?.forEach { edge ->
                            val product = edge?.node
                            Log.d(
                                "AmrApollo",
                                "Product: ${product?.title}, Description: ${product?.description}"
                            )
                        }
                    }

                    is ApiState.Error -> {
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }
    }
        fun hideBottomNavigation() {
            bottomNav.setVisibility(View.GONE)
        }

        fun showBottomNavigation() {
            bottomNav.setVisibility(View.VISIBLE)
        }


        override fun onSupportNavigateUp(): Boolean {
            val navController = findNavController(R.id.nav_host_fragment)
            return navController.navigateUp() || super.onSupportNavigateUp()
        }
}