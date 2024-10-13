package com.example.couturecorner.favorite.ui.view

import com.graphql.ProductQuery

interface OnFavoriteItemClickListener {
    fun onItemClick(product: ProductQuery.Product)
    fun onFavoriteClick(productId: String)
    fun currencySymbol(): String
}
