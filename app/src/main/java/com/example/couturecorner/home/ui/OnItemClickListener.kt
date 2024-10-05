package com.example.couturecorner.home.ui

import com.graphql.HomeProductsQuery

interface OnItemClickListener {
    fun onItemClick(product: HomeProductsQuery.Node?)
}
