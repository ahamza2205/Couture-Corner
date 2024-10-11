package com.example.couturecorner.order.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentOrdersBinding
import com.example.couturecorner.order.viewModel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrdersFragment : Fragment() {

    val viewModel: OrdersViewModel by viewModels()

    lateinit var binding: FragmentOrdersBinding

    lateinit var ordersAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ordersAdapter=OrderAdapter()
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.ordersRecyclerView.adapter = ordersAdapter

        viewModel.getOrders()

        lifecycleScope.launch {
            viewModel.orders.collect{
                when(it){
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        val orders = it.data?.data?.orders?.edges
                        ordersAdapter.submitList(orders)
//                        showLoading(false)
                    }
                    is ApiState.Error -> {
//                        showLoading(false)
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }

    }


}