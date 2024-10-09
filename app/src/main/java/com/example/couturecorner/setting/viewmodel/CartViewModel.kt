package com.example.couturecorner.setting.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apollographql.apollo3.api.Optional
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
private var userID:String? = null
    private var tag:String? = null
    // Prices
    private val deliveryFee = 5.0
    private val discount = 5.0

    private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

    init {
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                val customerId = repo.getShopifyUserId(userEmail)
                if (customerId != null) {
                    userID = customerId
                     tag = repo.getDraftOrderTag(userID!!)
                    Log.i("CartTag", "getFromSharedPref: "+tag.toString())

                }
                else {
                    Log.e("Cart", "Error: Customer ID is null")
                }
            }
        }
        else{
            Log.e("Cart", "Error: User is null")
        }
    }

    // ViewModel Function to Fetch Cart Items
    fun getCartItems() {

                if (userID != null) {

                    viewModelScope.launch {
                        try {
                            repo.getDraftOrderByCustomerId(userID!!)
                                .collect { response ->
                                    Log.i("CartTag", "getFromSharedPref: "+tag)

                                    val fetchedCartItems = cartItemMapper.mapToCartItems(response,
                                        tag.toString()
                                    )
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
                val newQuantity = maxOf(localItem.quantity ?: 0, remoteItem.inventoryQuantity ?: 0)
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

// Function to add an item by CartItem
    fun addedToCart(cartItem: CartItem) {
    viewModelScope.launch {
        // Check if a draft order ID exists for the user
        var draftOrderId = repo.getDraftOrderId(userID.toString())
        Log.i("CartTag", "getFromSharedPref: "+tag)

        // If the draft order ID is null, create a new draft order
        if (draftOrderId == null) {
            Log.i("CartTag", "getFromSharedPref: "+tag)

            repo.createDraftOrder(
                DraftOrderInput(customerId = Optional.present(userID)
                ,
                    tags = Optional.present(listOf(tag.toString())),
                    lineItems = Optional.present(listOf(   DraftOrderLineItemInput(
                        variantId = cartItem.id.toString(),
                        quantity = cartItem.quantity ?: 1 // Use the locally updated quantity
                    ))
                    )
                ))
                .collect { response ->
                    draftOrderId = response.data?.draftOrderCreate?.draftOrder?.id

                    Log.i("IDDRAFT", "addedToSharedPref:"+response.data)
                    Log.i("IDDRAFT", "addedToSharedPref:"+draftOrderId)

                    if (draftOrderId != null) {
                        repo.saveDraftOrderId(userID.toString(), draftOrderId.toString())
                        Log.i("Cart", "addedToSharedPref: "+draftOrderId)

                    }
                }
        }

        Log.i("Cart", "addedToCart: $draftOrderId")


        // First check if the item already exists in the local cart
        val existingItem = cartItemList.find { it.id == cartItem.id }

        if (existingItem != null) {
            // If it exists, we just increase its quantity
            val updatedItem = existingItem.copy(
                quantity = existingItem.quantity!! + cartItem.quantity!!  // Add to existing quantity
            )
            Log.i(
                "Cart",
                "Updated existing item: ${updatedItem.id} with new quantity: ${updatedItem.quantity}"
            )
            updateLocalCartItem(updatedItem)
        } else {
            // If it doesn't exist, add it as a new item
            val newItem = cartItem.copy() // Make sure to copy cartItem to avoid mutations
            Log.i("Cart", "Added new item: ${newItem.id} with quantity: ${newItem.quantity}")
            updateLocalCartItem(newItem)
        }

        // Sync the local cart list with the Shopify draft order
        updateShopifyDraftOrder(cartItemList)
    }

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
              var  draftOrderID=repo.getDraftOrderId(userID.toString())
                // Collecting the flow response properly
                repo.updateDraftOrder(
                    DraftOrderInput(
                        lineItems = Optional.present(draftOrderLineItems)
                    ),
                    draftOrderID.toString()

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