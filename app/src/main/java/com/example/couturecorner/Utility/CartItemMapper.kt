package com.example.couturecorner.Utility

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.CartItem
import com.graphql.GetDraftOrdersByCustomerQuery

class CartItemMapper {

    fun mapToCartItems(
        response: ApolloResponse<GetDraftOrdersByCustomerQuery.Data>,
        requiredTag: String
    ): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()
        response.data?.draftOrders?.nodes?.forEach { draftOrderNode ->
            // Check if the tags contain the requiredTag
            if (draftOrderNode.tags?.getOrNull(0) == requiredTag) {
                draftOrderNode.lineItems?.nodes?.forEach { lineItemNode ->
                    val variant = lineItemNode.variant
                    val product = variant?.product

                    val size = variant?.selectedOptions?.firstOrNull { it?.name == "Size" }?.value
                    val color = variant?.selectedOptions?.firstOrNull { it?.name == "Color" }?.value

                    val imageUrl = product?.media?.nodes
                        ?.firstOrNull { it.onMediaImage?.image?.url != null }
                        ?.onMediaImage?.image?.url
                    val correctName = lineItemNode.name?.split("|")?.firstOrNull()?.trim() ?: ""

                    val cartItem = CartItem(
                        id = variant?.id,
                        name = correctName,
                        quantity = lineItemNode.quantity,
                        price = (variant?.price?.toDouble() ?: 0.0).toString(),
                        imageUrl = imageUrl,
                        inventoryQuantity = variant?.inventoryQuantity,
                        size = size,
                        color = color
                    )
                    cartItems.add(cartItem)
                }
            }
        }
        return cartItems
    }
}