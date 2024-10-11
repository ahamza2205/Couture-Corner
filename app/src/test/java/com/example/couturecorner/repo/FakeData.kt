package com.example.couturecorner.repo


import com.graphql.AddFavoriteProductMutation
import com.graphql.GetFavoriteProductsQuery

object FakeData {
    fun getFakeProductQueryResponse(): GetFavoriteProductsQuery.Data {
        return GetFavoriteProductsQuery.Data(
            customer = GetFavoriteProductsQuery.Customer(
                metafields = GetFavoriteProductsQuery.Metafields(
                    edges = listOf(
                        GetFavoriteProductsQuery.Edge(
                            node = GetFavoriteProductsQuery.Node(
                                namespace = "favorites",
                                key = "wishlist",
                                value = "[\"123\"]"
                            )
                        )
                    )
                )
            )
        )
    }

    fun getFakeAddFavoriteMutationResponse(): AddFavoriteProductMutation.Data {
        return AddFavoriteProductMutation.Data(
            customerUpdate = AddFavoriteProductMutation.CustomerUpdate(
                customer = AddFavoriteProductMutation.Customer(
                    id = "123",
                    metafields = AddFavoriteProductMutation.Metafields(
                        edges = listOf(
                            AddFavoriteProductMutation.Edge(
                                node = AddFavoriteProductMutation.Node(
                                    namespace = "favorites",
                                    key = "wishlist",
                                    value = "[\"123\"]"
                                )
                            )
                        )
                    )
                ),
                userErrors = emptyList() // No errors for success
            )
        )
    }
}
