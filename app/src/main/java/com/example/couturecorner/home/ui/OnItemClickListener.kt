package com.example.couturecorner.home.ui

import com.graphql.FilteredProductsQuery

interface OnItemClickListener {
    fun onFavoriteClick(productId: String)
    fun onItemClick(product: FilteredProductsQuery.Node?)
    fun deleteFavorite(productId: String)
  //  fun isFavorite(productId: String): Boolean
}
