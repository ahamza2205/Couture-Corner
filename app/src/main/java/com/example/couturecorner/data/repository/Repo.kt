package com.example.couturecorner.data.repository

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.remote.IremoteData
import com.graphql.GetCuponCodesQuery
import com.graphql.GetProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.type.CustomerInput
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repo
    @Inject constructor(

                private val remoteData: IremoteData
) : Irepo {


    companion object
    {
        private var instance : Repo? = null

        fun getInstance(remoteData : IremoteData): Repo
        {
            return instance ?: synchronized(this)
            {
                val temp = Repo(remoteData)
                instance =temp
                temp
            }
        }
    }


    override fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>> {
        return remoteData.getProducts()
    }

    override fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>> {
        return remoteData.getCupones()
    }
    override fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>> {
        return remoteData.updateCustomer(input)
    }
}