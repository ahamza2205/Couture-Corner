package com.example.couturecorner.setting.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.repository.Repo
import com.graphql.type.CustomerInput
import com.graphql.type.MailingAddressInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAdressViewModel @Inject constructor(
    private val repo: Repo
) : ViewModel() {
    // LiveData to observe the response state
    private val _updateStatus = MutableLiveData<Result<String>>()
    val updateStatus: LiveData<Result<String>> get() = _updateStatus


    // Function to update the customer with the address
    fun updateAddressCustomer(address: List<MailingAddressInput>,userId: String) {


                    val input = CustomerInput(
                        id = Optional.Present(userId),
                        addresses = Optional.present(address)
                    )

                    viewModelScope.launch {
                        Log.i("AddAddress", "updateCustomer: $userId")
                        repo.updateCustomer(input).collect { response ->
                            if (response.hasErrors()) {
                                val errorMsg = response.errors?.get(0)?.message ?: "Unknown Error"
                                _updateStatus.postValue(Result.failure(Exception(errorMsg)))
                                Log.i("AddAddress", "updateCustomer: $errorMsg")
                            } else {
                                response.data?.customerUpdate?.customer?.id?.let { updatedCustomerId ->
                                    _updateStatus.postValue(Result.success(updatedCustomerId))
                                }
                                    ?: _updateStatus.postValue(Result.failure(Exception("Customer ID is null")))
                            }
                        }
                    }
                }



}