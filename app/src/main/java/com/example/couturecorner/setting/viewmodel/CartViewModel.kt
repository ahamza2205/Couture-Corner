package com.example.couturecorner.setting.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.couturecorner.R

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems
    // To observe the calculated total price
    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice
private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal
    private val deliveryFee = 5.0
    private val discount = 5.0

    init {
        // Initialize the list with mock data
        _cartItems.value = generateMockCartItems()
        calculateTotal() // Calculate initial total

    }

    // Method to generate mock cart items
    private fun generateMockCartItems(): List<CartItem> {
        return listOf(
            CartItem(R.drawable.test, "Item 1", 25, 1),
            CartItem(R.drawable.test, "Item 2", 30, 2),
            CartItem(R.drawable.test, "Item 3", 15, 3),
            CartItem(R.drawable.test, "Item 4", 50, 1),
            CartItem(R.drawable.test, "Item 5", 20, 2),
            CartItem(R.drawable.test, "Item 6", 35, 3),
            CartItem(R.drawable.test, "Item 7", 40, 1),
            CartItem(R.drawable.test, "Item 8", 45, 2),
        )
    }

    // Method to increase quantity
    fun increaseQuantity(cartItem: CartItem) {
        _cartItems.value?.let { currentItems ->
            val updatedItems = currentItems.map {
                if (it == cartItem) it.copy(quantity = it.quantity + 1) else it
            }
            _cartItems.value = updatedItems
            calculateTotal()

        }
    }

    // Method to decrease quantity
    fun decreaseQuantity(cartItem: CartItem) {
        _cartItems.value?.let { currentItems ->
            val updatedItems = currentItems.map {
                if (it == cartItem && it.quantity > 0) it.copy(quantity = it.quantity - 1) else it
            }
            _cartItems.value = updatedItems
            calculateTotal()
        }
    }
    private fun calculateTotal() {
        val currentItems = _cartItems.value ?: emptyList()

        val subtotal = currentItems.sumOf { it.quantity * it.price.toDouble() }

        _subtotal.value = subtotal
        val total = subtotal + deliveryFee - discount

        _totalPrice.value = total
    }

}


// Data class for CartItem (should be outside of the ViewModel class)
data class CartItem(
    val imageResId: Int,
    val name: String,
    val price: Int,
    val quantity: Int
)
