package com.example.couturecorner.home.viewmodel

import com.example.couturecorner.data.repository.Irepo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.local.SharedPreferenceImp
import com.example.couturecorner.data.model.ApiState
import com.graphql.GetCuponCodesQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel

class CuponsVeiwModel @Inject constructor(
    private val repo: Irepo,
    val sharedPreference: SharedPreferenceImp
): ViewModel() {

    private val _cupons= MutableStateFlow<ApiState<ApolloResponse<GetCuponCodesQuery.Data>>>(
        ApiState.Loading)
    val cupons : StateFlow<ApiState<ApolloResponse<GetCuponCodesQuery.Data>>> =_cupons


    fun getCupons()
    {
        viewModelScope.launch {
            repo.getCupones().collect {
                if (it.hasErrors())
                {
                    _cupons.value= ApiState.Error(it.errors?.get(0)?.message.toString())
                }
                else
                {
                    _cupons.value= ApiState.Success(it)
                }
            }
        }
    }



}