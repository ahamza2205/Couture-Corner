package com.example.couturecorner.order.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentOrdersBinding
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.order.viewModel.OrdersViewModel
import com.graphql.FilteredProductsQuery
import com.graphql.GetOrdersByCustomerQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrdersFragment : Fragment() {

    val viewModel: OrdersViewModel by viewModels()
    val sharedViewModel:MainViewModel by activityViewModels()

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

        ordersAdapter=OrderAdapter (
            myListenner = {
                val reviewBottomSheet = OrderDetailFragment.newInstance(it?:"")
                reviewBottomSheet.show(childFragmentManager, "ReviewBottomSheet")},
            listennerForCurenncy = {getCurrencySymbol(sharedViewModel.getSelectedCurrency() ?: "EGP") }
        )
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.ordersRecyclerView.adapter = ordersAdapter

        viewModel.getOrders()

        lifecycleScope.launch {
            viewModel.orders.collect{
                when(it){
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        val orders = it.data?.data?.orders?.edges
                        prepareProductsForAdapter(orders ?: emptyList())
//                        ordersAdapter.submitList(orders)
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

    private fun prepareProductsForAdapter(orders: List<GetOrdersByCustomerQuery.Edge?>) {
        if (orders.isNotEmpty())
        {
            val updatedProducts = orders.map { order ->
                val orderId = order?.node?.name
                val originalPrice = order?.node?.totalPriceSet?.shopMoney?.amount?.toDoubleOrNull() ?: 0.0

                // Trigger conversion for each product
                sharedViewModel.convertCurrency("EGP", sharedViewModel.getSelectedCurrency() ?: "EGP", originalPrice, orderId ?: "")

                // Return the original product, the price will be updated later
                order
            }

            // Observe currency conversion updates
            lifecycleScope.launch {
                sharedViewModel.convertedCurrency.collect { conversions ->
                    val updatedList = updatedProducts.map { order  ->
                        val orderId = order?.node?.name
                        val convertedPrice = conversions[orderId] ?:order?.node?.totalPriceSet?.shopMoney?.amount?.toDoubleOrNull() ?: 0.0

                        val price = order?.node?.totalPriceSet?.shopMoney?.amount
                        // Create a copy or modify the product item with the new price
                        order?.copy(
                            node = order.node?.copy(
                                totalPriceSet = order.node.totalPriceSet?.copy(
                                    shopMoney = order.node.totalPriceSet.shopMoney?.copy(
                                        amount = convertedPrice.toString()
                                    )
                                )
                            )
                        )


                    }
                    Log.d("CheckForConversion", "${updatedList[0]?.node?.totalPriceSet?.shopMoney?.amount}: ")

                    // Submit the updated list with converted prices to the adapter
//                showLoading(false)
                    ordersAdapter.submitList(updatedList)
                }
            }
        }
        else
        {
            ordersAdapter.submitList(orders)
        }
    }


//    fun getSymbol():String
//    {
//        return getCurrencySymbol(sharedViewModel.getSelectedCurrency() ?: "EGP")
//    }

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