package com.example.couturecorner.order.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentOrderDetailsBinding
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.example.couturecorner.order.viewModel.OrdersViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.OrderByIdQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentOrderDetailsBinding
    lateinit var orderProductsAdapter: OrderItemAdapter

    private var orderId: String? = null

    val viewModel:OrdersViewModel by viewModels()
    val sharedViewModel:MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            orderId = it.getString(ARG_PASSED_STRING)
        }

        binding= FragmentOrderDetailsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderProductsAdapter= OrderItemAdapter(){getCurrencySymbol(sharedViewModel.getSelectedCurrency()?: "EGP")}
        binding.orderItemsRecyclerView.adapter=orderProductsAdapter
        binding.orderItemsRecyclerView.layoutManager= LinearLayoutManager(context)

        Log.d("AmrTestOrderID", "$orderId")
        viewModel.getOrderById(orderId?:"")

        lifecycleScope.launch {
            viewModel.ordersId.collect{
                when(it){
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {

                        binding.addressTextView.text=getString(
                            R.string.adress,
                            it.data?.data?.order?.billingAddress?.address1,
                            it.data?.data?.order?.billingAddress?.city)

                        sharedViewModel.covertCurrencyWithoutId("EGP",sharedViewModel.getSelectedCurrency() ?: "EGP",
                            it.data?.data?.order?.totalPriceSet?.shopMoney?.amount?.toDoubleOrNull() ?: 0.0)

                        val orders = it.data?.data?.order?.lineItems?.edges
                        prepareProductsForAdapter(orders ?: emptyList())
//                        showLoading(false)
                    }
                    is ApiState.Error -> {
//                        showLoading(false)
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }

        lifecycleScope.launch {
            sharedViewModel.convertedCurrencyTotal.collect{
                binding.totalPriceTextView.text=getString(
                            R.string.Totalprice,
                           it.toString(),
                           getCurrencySymbol(sharedViewModel.getSelectedCurrency() ?: "EGP"))
            }
        }


    }


    private fun prepareProductsForAdapter(items: List<OrderByIdQuery.Edge?>) {
        if (items.isNotEmpty())
        {
            val updatedProducts = items.map { order ->
                val orderId = order?.node?.name
                val originalPrice = order?.node?.originalUnitPriceSet?.shopMoney?.amount?.toDoubleOrNull() ?: 0.0

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
                        val convertedPrice = conversions[orderId] ?:order?.node?.originalUnitPriceSet?.shopMoney?.amount?.toDoubleOrNull() ?: 0.0

                        val price = order?.node?.originalUnitPriceSet?.shopMoney?.amount
                        // Create a copy or modify the product item with the new price
                        order?.copy(
                            node = order.node?.copy(
                                originalUnitPriceSet = order.node.originalUnitPriceSet?.copy(
                                    shopMoney = order.node.originalUnitPriceSet.shopMoney?.copy(
                                        amount = convertedPrice.toString()
                                    )
                                )
                            )
                        )


                    }
                    Log.d("CheckForConversion", "${updatedList[0]?.node?.originalUnitPriceSet?.shopMoney?.amount}: ")

                    // Submit the updated list with converted prices to the adapter
//                showLoading(false)
                    orderProductsAdapter.submitList(updatedList)
                }
            }
        }
        else
        {
            orderProductsAdapter.submitList(items)
        }
    }



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

    companion object {
        private const val ARG_PASSED_STRING = "passed_string"


        fun newInstance(passedString: String): OrderDetailFragment {
            val fragment = OrderDetailFragment()
            val args = Bundle()
            args.putString(ARG_PASSED_STRING, passedString)
            fragment.arguments = args
            return fragment
        }
    }
}