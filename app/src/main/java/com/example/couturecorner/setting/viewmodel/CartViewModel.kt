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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class CartViewModel@Inject constructor(
    private val repo: Repo
):ViewModel() {
    private val _updatCartListStatus = MutableLiveData<Result<List<CartItem>>>()
    val uppdatCartListStatus: LiveData<Result<List<CartItem>>> get() = _updatCartListStatus
    private val cartItemMapper = CartItemMapper()


    private val user = FirebaseAuth.getInstance().currentUser
    fun getCartItems() {
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                // Get the Shopify customer ID using the email
                val customerId = repo.getShopifyUserId(userEmail)
                if (customerId != null) {
//                    val tag = repo.getDraftOrderTag(customerId)
val tag = "gid://shopify/Customer/8417368834332"

                    // Call the repository to fetch draft orders and update LiveData
                    viewModelScope.launch {
                        try {
                            repo.getDraftOrderByCustomerId(customerId)
                                .collect { response ->
//                                    Log.i("Cart", "getCartItems: " + response.data)
                                    val cartItems = cartItemMapper.mapToCartItems(response, tag)
                                    Log.i("Cart", "getCartItems: "+cartItems)
                                    _updatCartListStatus.postValue(Result.success(cartItems))
                                }
                        } catch (e: Exception) {
                            Log.i("Cart", "getCartItems: " + e)

                            _updatCartListStatus.postValue(Result.failure(e))
                        }
                    }
                } else {
                    Log.i("Cart", "User ID is null")
                }
            }
        }
    }



}


    // To observe the calculated total price
    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice
private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal
    private val deliveryFee = 5.0
    private val discount = 5.0

//    init {
//        calculateTotal() // Calculate initial total
//
//    }


    // Method to increase quantity
//    fun increaseQuantity(cartItem: CartItem) {
//        _cartItems.value?.let { currentItems ->
//            val updatedItems = currentItems.map {
//                if (it == cartItem) it.copy(quantity = it.quantity + 1) else it
//            }
//            _cartItems.value = updatedItems
//            calculateTotal()
//
//        }
//    }

    // Method to decrease quantity
//    fun decreaseQuantity(cartItem: CartItem) {
//        _cartItems.value?.let { currentItems ->
//            val updatedItems = currentItems.map {
//                if (it == cartItem && it.quantity??.toInt() > 0) it.copy(quantity = it.quantity - 1) else it
//            }
//            _cartItems.value = updatedItems
//            calculateTotal()
//        }
//    }
//    private fun calculateTotal() {
//        val currentItems = _cartItems.value ?: emptyList()
//
//        val subtotal = currentItems.sumOf { it.quantity * it.price.toDouble() }
//
//        _subtotal.value = subtotal
//        val total = subtotal + deliveryFee - discount
//
//        _totalPrice.value = total
//    }




