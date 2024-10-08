package com.example.couturecorner.setting.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.Utility.CartItemMapper
import com.example.couturecorner.data.model.CartItem
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import com.graphql.type.DraftOrderInput
import com.graphql.type.DraftOrderLineItemInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repo: Repo
) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _updateCartStatus = MutableLiveData<Result<List<CartItem>>>()
    val updateCartStatus: LiveData<Result<List<CartItem>>> get() = _updateCartStatus

    private val cartItemMapper = CartItemMapper()
    private val user = FirebaseAuth.getInstance().currentUser
    private val cartItemList = mutableListOf<CartItem>()

    // Prices
    private val deliveryFee = 5.0
    private val discount = 5.0

    private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice


    // ViewModel Function to Fetch Cart Items
    fun getCartItems() {
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                val customerId = repo.getShopifyUserId(userEmail)
                if (customerId != null) {
                    val tag = "gid://shopify/Customer/8417368834332"
                    viewModelScope.launch {
                        try {
                            repo.getDraftOrderByCustomerId(customerId)
                                .collect { response ->
                                    val fetchedCartItems = cartItemMapper.mapToCartItems(response, tag)
                                    Log.i("Cart", "Fetched cart items: $fetchedCartItems")

                                    val mergedCartItems = mergeLocalAndRemoteCartItems(fetchedCartItems)

                                    // Log after merging
                                    Log.i("Cart", "Merged Cart Items: $mergedCartItems")

                                    _updateCartStatus.postValue(Result.success(mergedCartItems))
                                    _cartItems.postValue(mergedCartItems)

                                    cartItemList.clear()
                                    cartItemList.addAll(mergedCartItems)

                                    // Calculate and update the total price
                                    calculateTotal()
                                }
                        } catch (e: Exception) {
                            Log.e("Cart", "Error fetching cart items: $e")
                            _updateCartStatus.postValue(Result.failure(e))
                        }
                    }
                } else {
                    Log.i("Cart", "User ID is null")
                }
            }
        }
    }

    // Function to Merge Local and Remote Cart Items
// Function to Merge Local and Remote Cart Items
    private fun mergeLocalAndRemoteCartItems(remoteCartItems: List<CartItem>): List<CartItem> {
        val updatedList = mutableListOf<CartItem>()
        remoteCartItems.forEach { remoteItem ->
            val localItem = cartItemList.find { it.id == remoteItem.id }
            if (localItem != null) {
                // Use the maximum quantity between local and remote
                val newQuantity = maxOf(localItem.quantity ?: 0, remoteItem.quantity ?: 0)
                val updatedItem = localItem.copy(quantity = newQuantity)
                updatedList.add(updatedItem)
                cartItemList.remove(localItem) // Remove old item
            } else {
                updatedList.add(remoteItem)
            }
        }
        // Add any remaining local items that were not in the remote list
        updatedList.addAll(cartItemList)
        return updatedList
    }
    // Increase quantity of a cart item
    fun increaseQuantity(cartItem: CartItem) {
        val updatedItem = cartItem.copy(quantity = cartItem.quantity!! + 1)
        Log.i("Cart", "Increasing quantity for item: ${cartItem.id}, new quantity: ${updatedItem.quantity}")
        updateLocalCartItem(updatedItem)
        updateShopifyDraftOrder(cartItemList)
    }

    // Decrease quantity of a cart item
    fun decreaseQuantity(cartItem: CartItem) {
        val newQuantity = (cartItem.quantity!!) - 1
        if (newQuantity > 0) {
            val updatedItem = cartItem.copy(quantity = newQuantity)
            Log.i("Cart", "Decreasing quantity for item: ${cartItem.id}, new quantity: $newQuantity")
            updateLocalCartItem(updatedItem)
        } else {
            Log.i("Cart", "Removing item: ${cartItem.id} as quantity reached zero")
            cartItemList.remove(cartItem)
        }
        updateShopifyDraftOrder(cartItemList)
    }

    fun onDeleteCartItem(cartItem: CartItem) {
        // Remove the item from the local cart list
        if (cartItemList.remove(cartItem)) {
            Log.i("Cart", "Removed item from local cart: ${cartItem.id}")

            // Update LiveData to notify observers
            _cartItems.postValue(cartItemList.toList())

            // Recalculate total after deletion
            calculateTotal()

            // Update the Shopify draft order
            updateShopifyDraftOrder(cartItemList)
        } else {
            Log.i("Cart", "Item not found in local cart: ${cartItem.id}")
        }
    }

    // Update the local cart and post new values to LiveData
    private fun updateLocalCartItem(item: CartItem) {
        val index = cartItemList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            cartItemList[index] = item
            Log.i("Cart", "Updated local cart item: ${item.id} with quantity: ${item.quantity}")
        } else {
            cartItemList.add(item)
            Log.i("Cart", "Added new item to local cart: ${item.id} with quantity: ${item.quantity}")
        }

        _cartItems.postValue(cartItemList.toList())
        calculateTotal()
    }

    // Calculate the subtotal and total price
    private fun calculateTotal() {
        val currentItems = cartItemList
        val subtotal = currentItems.sumOf {
            (it.quantity ?: 0) * (it.price?.toDoubleOrNull() ?: 0.0)
        }

        Log.i("Cart", "Calculating subtotal: $subtotal")
        _subtotal.value = subtotal
        _totalPrice.value = subtotal + deliveryFee - discount
        Log.i("Cart", "Total price after applying delivery fee and discount: ${_totalPrice.value}")
    }

    // Update Shopify draft order with new cart items
    private fun updateShopifyDraftOrder(cartItems: List<CartItem>) {
        val draftOrderLineItems = cartItems.map {
            DraftOrderLineItemInput(
                variantId = it.id.toString(),
                quantity = it.quantity ?: 1 // Use the locally updated quantity
            )
        }

        viewModelScope.launch {
            try {
                // Collecting the flow response properly
                repo.updateDraftOrder(
                    DraftOrderInput(
                        lineItems = draftOrderLineItems
                    ),
                    "gid://shopify/DraftOrder/1166554792220" // Replace with the correct Draft Order ID
                ).collect { response ->
                    Log.i("Cart", "updateShopifyDraftOrder: Response - ${response.data}")

                    // After updating, fetch the updated cart items to ensure they reflect the changes
                    getCartItems()
                }
            } catch (e: Exception) {
                Log.e("Cart", "Error updating draft order: $e")
            }
        }
    }
}