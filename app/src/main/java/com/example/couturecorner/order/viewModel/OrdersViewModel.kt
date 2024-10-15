package com.example.couturecorner.order.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.google.firebase.auth.FirebaseAuth
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.OrderByIdQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
   private val repo:Irepo
):ViewModel() {
   private val _orders= MutableStateFlow<ApiState<ApolloResponse<GetOrdersByCustomerQuery.Data>>>(ApiState.Loading)
   val orders: StateFlow<ApiState<ApolloResponse<GetOrdersByCustomerQuery.Data>>> = _orders

   private val _ordersId= MutableStateFlow<ApiState<ApolloResponse<OrderByIdQuery.Data>>>(ApiState.Loading)
   val ordersId: StateFlow<ApiState<ApolloResponse<OrderByIdQuery.Data>>> = _ordersId

   fun getOrders(){

      val user = FirebaseAuth.getInstance().currentUser
      if (user != null) {
         val userEmail = user.email

         if (userEmail != null) {
            viewModelScope.launch {
               repo.getOrders(userEmail).collect {
                  if (it.hasErrors())
                  {
                     _orders.value= ApiState.Error(it.errors?.get(0)?.message.toString())
                  }
                  else
                  {
                     _orders.value= ApiState.Success(it)
                  }
               }
            }

         }
      }

   }

   fun getOrderById(id:String){
      viewModelScope.launch {
         repo.getOrderById(id).collect {
            if (it.hasErrors())
            {
               _ordersId.value= ApiState.Error(it.errors?.get(0)?.message.toString())
            }
            else
            {
               _ordersId.value= ApiState.Success(it)
            }
         }
      }
   }

}