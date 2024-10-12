package com.example.couturecorner.order.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.R
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentOrderDetailsBinding
import com.example.couturecorner.order.viewModel.OrdersViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentOrderDetailsBinding
    lateinit var orderProductsAdapter: OrderItemAdapter

    private var orderId: String? = null

    val viewModel:OrdersViewModel by viewModels()

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
        orderProductsAdapter= OrderItemAdapter()
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

                        binding.totalPriceTextView.text=getString(
                            R.string.Totalprice,
                            it.data?.data?.order?.totalPriceSet?.shopMoney?.amount,
                            it.data?.data?.order?.totalPriceSet?.shopMoney?.currencyCode)

                        val orders = it.data?.data?.order?.lineItems?.edges
                        orderProductsAdapter.submitList(orders)
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