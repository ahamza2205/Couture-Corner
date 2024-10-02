package com.example.couturecorner.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.couturecorner.R
import com.example.couturecorner.model.ApiState
import com.example.couturecorner.model.Repo
import com.example.couturecorner.model.remote.RemoteData
import com.example.couturecorner.viewModel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var factory: MainViewModelFactory
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

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
}