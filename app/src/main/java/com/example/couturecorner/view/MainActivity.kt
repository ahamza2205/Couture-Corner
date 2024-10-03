package com.example.couturecorner.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.couturecorner.R
import com.example.couturecorner.model.ApiState
import com.example.couturecorner.model.Repo
import com.example.couturecorner.model.remote.RemoteData
import com.example.couturecorner.viewModel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var factory: MainViewModelFactory
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        NavigationUI.setupActionBarWithNavController(this, navController)

        factory = MainViewModelFactory(Repo.getInstance(RemoteData()))
        viewModel = ViewModelProvider(this,factory).get(MainViewModel::class.java)


        viewModel.getProducts()

        lifecycleScope.launch {
            viewModel.productsApollo.collect{
                when(it){
                    is ApiState.Loading->{}
                    is ApiState.Success->{
                        val products = it.data.data?.products?.edges
                        products?.forEach { edge ->
                            val product = edge?.node
                            Log.d("AmrApollo", "Product: ${product?.title}, Description: ${product?.description}")
                        }
                    }
                    is ApiState.Error->{
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}