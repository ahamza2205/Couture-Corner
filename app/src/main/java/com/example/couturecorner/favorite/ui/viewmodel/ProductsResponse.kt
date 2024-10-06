package com.example.couturecorner.favorite.ui.viewmodel

import com.graphql.ProductQuery

data class ProductsResponse(val products: List<ProductQuery.Product>)
