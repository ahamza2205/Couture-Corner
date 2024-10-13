package com.example.couturecorner.setting.viewmodel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.Utility.CartItemMapper
import com.example.couturecorner.data.model.Address
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.model.CartItem
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import com.graphql.type.DraftOrderAppliedDiscountInput
import com.graphql.type.DraftOrderAppliedDiscountType
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput
import com.graphql.type.DraftOrderLineItemInput
import com.graphql.type.MailingAddressInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repo: Repo
) : ViewModel() {
    private val _draftOrderStatus = MutableLiveData<ApiState<Any>>()
    val draftOrderStatus: LiveData<ApiState<Any>> = _draftOrderStatus

    private val _addTOCartState = MutableLiveData<ApiState<Any>>()
    val addToCartState: LiveData<ApiState<Any>> = _addTOCartState
    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address> = _address

    fun setAddress(address: Address) {
        _address.value = address
        Log.i("Final", "setAddress: " + address)
    }

    private val _cartItems = MutableLiveData<ApiState<List<CartItem>>>()
    val cartItems: LiveData<ApiState<List<CartItem>>> get() = _cartItems

    private val _updateCartStatus = MutableLiveData<ApiState<List<CartItem>>>()
    val updateCartStatus: LiveData<ApiState<List<CartItem>>> get() = _updateCartStatus

    private val cartItemMapper = CartItemMapper()
    private val user = FirebaseAuth.getInstance().currentUser
    private val cartItemList = mutableListOf<CartItem>()
    private var userID: String? = null
    private var tag: String? = null

    // Prices

    private val _discount = MutableLiveData<Double>(0.0) // Default value
    val discount: LiveData<Double> = _discount


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
                    getCartItems()

                }
            }
        }
    }

    // ViewModel Function to Fetch Cart Items
    fun getCartItems() {

        if (userID != null) {
            _cartItems.postValue(ApiState.Loading)

            viewModelScope.launch {
                try {
                    repo.getDraftOrderByCustomerId(userID!!)
                        .collect { response ->

                            val fetchedCartItems = cartItemMapper.mapToCartItems(
                                response,
                                tag.toString()
                            )

                            val mergedCartItems = mergeLocalAndRemoteCartItems(fetchedCartItems)
                            _cartItems.postValue(ApiState.Success(mergedCartItems))
                            _updateCartStatus.postValue(ApiState.Success(mergedCartItems))


                            cartItemList.clear()
                            cartItemList.addAll(mergedCartItems)
                            // Calculate and update the total price
                            calculateTotal()
                        }
                } catch (e: Exception) {
                    Log.e("Cart", "Error fetching cart items: $e")
                    _updateCartStatus.postValue(ApiState.Error("Error fetching cart items: $e"))
                }
            }
        }
    }


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

        updateLocalCartItem(updatedItem)
        updateShopifyDraftOrder(cartItemList)
    }

    // Decrease quantity of a cart item
    fun decreaseQuantity(cartItem: CartItem) {
        val newQuantity = (cartItem.quantity!!) - 1
        if (newQuantity > 0) {
            val updatedItem = cartItem.copy(quantity = newQuantity)

            updateLocalCartItem(updatedItem)
        } else {
            cartItemList.remove(cartItem)
        }
        updateShopifyDraftOrder(cartItemList)
    }

    fun onDeleteCartItem(cartItem: CartItem) {
        // Remove the item from the local cart list
        if (cartItemList.remove(cartItem)) {
            _cartItems.postValue(ApiState.Success(cartItemList.toList()))
            calculateTotal()
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
        } else {
            cartItemList.add(item)
        }
        _cartItems.postValue(ApiState.Success(cartItemList.toList())) // Post updated list to LiveData
        calculateTotal() // Recalculate total price
    }

    // Function to add an item by CartItem
    fun addedToCart(cartItem: CartItem) {
        viewModelScope.launch {
            _addTOCartState.postValue(ApiState.Loading)

            var isSuccessPosted = false // Flag to ensure only one Success is posted

            // Check if a draft order ID exists for the user
            var draftOrderId = repo.getDraftOrderId(userID.toString())

            // If the draft order ID is null, create a new draft order
            if (draftOrderId == null) {
                repo.createDraftOrder(
                    DraftOrderInput(
                        customerId = Optional.present(userID),
                        tags = Optional.present(listOf(tag.toString())),
                        lineItems = Optional.present(
                            listOf(
                                DraftOrderLineItemInput(
                                    variantId = cartItem.id.toString(),
                                    quantity = cartItem.quantity ?: 1 // Use the locally updated quantity
                                )
                            )
                        )
                    )
                ).collect { response ->
                    draftOrderId = response.data?.draftOrderCreate?.draftOrder?.id
                    if (draftOrderId != null) {
                        repo.saveDraftOrderId(userID.toString(), draftOrderId.toString())
                        if (!isSuccessPosted) {
                            _addTOCartState.postValue(ApiState.Success(""))
                            isSuccessPosted = true // Mark as success posted
                        }
                    }
                }
            }

            // First check if the item already exists in the local cart
            val existingItem = cartItemList.find { it.id == cartItem.id }

            if (existingItem != null) {
                // If it exists, we just increase its quantity
                val updatedItem = existingItem.copy(
                    quantity = existingItem.quantity!! + cartItem.quantity!! // Add to existing quantity
                )
                updateLocalCartItem(updatedItem)
                if (!isSuccessPosted) {
                    _addTOCartState.postValue(ApiState.Success(""))
                    isSuccessPosted = true // Mark as success posted
                }
            } else {
                // If it doesn't exist, add it as a new item
                val newItem = cartItem.copy() // Make sure to copy cartItem to avoid mutations
                updateLocalCartItem(newItem)
                if (!isSuccessPosted) {
                    _addTOCartState.postValue(ApiState.Success(""))
                    isSuccessPosted = true // Mark as success posted
                }
            }

            // Sync the local cart list with the Shopify draft order
            updateShopifyDraftOrder(cartItemList)
        }
    }
//get discount
fun setDiscount(newDiscount: Double) {
    _discount.value = newDiscount
    calculateTotal()
}


    // Calculate the subtotal and total price with discount as a percentage
    private fun calculateTotal() {
        val currentItems = cartItemList
        // Calculate the subtotal by summing up the price and quantity of each item
        val subtotal = currentItems.sumOf {
            (it.quantity ?: 0) * (it.price?.toDoubleOrNull() ?: 0.0)
        }
        _subtotal.value = subtotal

        // Treat discount as a percentage, e.g., if discount is 10.0, it's 10%
        val discountPercentage = (discount.value ?: 0.0) / 100.0
        val discountAmount = subtotal * discountPercentage

        // Calculate total price after applying the discount
        _totalPrice.value = subtotal - discountAmount
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
                var draftOrderID = repo.getDraftOrderId(userID.toString())
                // Collecting the flow response properly
                repo.updateDraftOrder(
                    DraftOrderInput(
                        lineItems = Optional.present(draftOrderLineItems)
                    ),
                    draftOrderID.toString()

                ).collect { response ->

                    // After updating, fetch the updated cart items to ensure they reflect the changes
                    getCartItems()
                }
            } catch (e: Exception) {
            }

        }

    }

        //create order from draft order
        fun createDraftOrder(cartItems: ApiState<List<CartItem>>) {
            if (cartItems !is ApiState.Success) {
                // Handle the case when cartItems are not successfully fetched
                _draftOrderStatus.postValue(ApiState.Error("Cart items not available"))
                return
            }

            val draftOrderLineItems = cartItems.data?.map {
                DraftOrderLineItemInput(
                    variantId = it.id.toString(),
                    quantity = it.quantity ?: 1 // Use the locally updated quantity
                )
            }

            // Set loading state before making the API call
            _draftOrderStatus.postValue(ApiState.Loading)

            viewModelScope.launch {
                try {
                    val response = repo.createDraftOrder(
                        DraftOrderInput(
                            customerId = Optional.present(userID),
                            lineItems = Optional.present(draftOrderLineItems),
                            billingAddress = Optional.present(
                                MailingAddressInput(
                                    address1 = address.value!!.name,
                                    address2 = address.value!!.addressDetails,
                                    city = address.value!!.city,
                                    phone = address.value!!.phone
                                )
                            ),
                            appliedDiscount = Optional.present(
                                DraftOrderAppliedDiscountInput(
                                    valueType = Optional.present(DraftOrderAppliedDiscountType.PERCENTAGE),
                                    value = Optional.present(discount.value?: 0.0)
                                )
                            )
                        )
                    )

                    response.collect { draftOrderResponse ->
                        val finalDraftOrderId = draftOrderResponse.data?.draftOrderCreate?.draftOrder?.id

                        if (finalDraftOrderId != null) {
                            // Proceed to create the final order from the draft
                            repo.createOrderFromDraft(finalDraftOrderId).collect { orderResponse ->
                                Log.i("Final Draft", "Order Response Data: ${orderResponse.data}")

                                if (orderResponse.data?.draftOrderComplete?.draftOrder?.id != null) {
                                    // Successful order creation

                                    // Delete draft order after successful creation
                                    val deleteInput = DraftOrderDeleteInput(finalDraftOrderId)
                                    repo.deleteDraftOrder(deleteInput).collect { deleteResponse ->
                                        Log.i("Final Draft", "Deleted Draft Order: ${deleteResponse.data}")

                                    }


                                    // Clear SharedPreferences for the draft order ID
                                    val sharedPrefDraftOrderId = repo.getDraftOrderId(userID.toString())
                                    if (sharedPrefDraftOrderId != null) {
                                        val sharedPrefDeleteInput = DraftOrderDeleteInput(sharedPrefDraftOrderId)
                                        repo.deleteDraftOrder(sharedPrefDeleteInput).collect { sharedPrefResponse ->
                                            Log.i("Final Draft", "Deleted Shared Pref Draft Order: ${sharedPrefResponse.data}")
                                        }
                                    }

                                    repo.deleteDraftOrderId(userID.toString())
                                    _draftOrderStatus.postValue(ApiState.Success("Draft Order created successfully."))

                                } else {
                                    // Order creation failed
                                    _draftOrderStatus.postValue(ApiState.Error("Order creation failed"))
                                }
                            }
                        } else {
                            // Draft Order creation failed
                            _draftOrderStatus.postValue(ApiState.Error("Draft Order creation failed"))
                        }
                    }
                } catch (e: Exception) {
                    // Handle errors and update the state with an error message
                    Log.e("Final", "Error creating draft order: $e")
                    _draftOrderStatus.postValue(ApiState.Error("Error creating draft order: $e"))
                }
            }
        }


}
