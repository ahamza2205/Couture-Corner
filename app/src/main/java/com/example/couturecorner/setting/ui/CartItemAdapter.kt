package com.example.couturecorner.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.databinding.CartItemBinding
import com.example.couturecorner.setting.viewmodel.CartItem

class CartItemAdapter(
    private val onIncreaseQuantity: (CartItem) -> Unit,
    private val onDecreaseQuantity: (CartItem) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    private val cartItems = mutableListOf<CartItem>()

    inner class CartItemViewHolder(val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            binding.imageView.setImageResource(cartItem.imageResId)
            binding.nameItem.text = cartItem.name
            binding.priceItem.text = cartItem.price.toString()
            binding.quanityTextView.text = cartItem.quantity.toString()

            // Handle add and remove click listeners
            binding.addImageView.setOnClickListener {
                onIncreaseQuantity(cartItem)
            }

            binding.removeImageView.setOnClickListener {
                onDecreaseQuantity(cartItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    fun updateCartItems(newCartItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newCartItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}
