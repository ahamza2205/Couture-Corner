package com.example.couturecorner.home.ui

import com.graphql.HomeProductsQuery

interface OnItemClickListener {
    fun onFavoriteClick(productId: String)
    fun onItemClick(product: HomeProductsQuery.Node?)
}
