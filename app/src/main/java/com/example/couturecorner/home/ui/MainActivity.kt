package com.example.couturecorner.home.ui
import android.os.Bundle
import android.util.Log
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
     val  viewModel: MainViewModel by  viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        NavigationUI.setupActionBarWithNavController(this, navController)

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
