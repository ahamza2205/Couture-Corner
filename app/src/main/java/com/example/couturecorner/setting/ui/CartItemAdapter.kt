package com.example.couturecorner.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.couturecorner.R
import com.example.couturecorner.data.model.CartItem
import com.example.couturecorner.databinding.CartItemBinding

class CartItemAdapter(
//    private val onIncreaseQuantity: (CartItem) -> Unit,
//    private val onDecreaseQuantity: (CartItem) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    private val cartItems = mutableListOf<CartItem>()

    inner class CartItemViewHolder(val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            Glide.with(binding.imageView.context)
                .load( cartItem.imageUrl)
                .placeholder(R.drawable.cart) // Placeholder while loading
                .error(R.drawable.cart) // Error image in case of failure
                .into(binding.imageView)
            binding.nameItem.text = cartItem.name
            binding.priceItem.text = cartItem.price.toString()
            binding.quanityTextView.text = cartItem.quantity.toString()
            binding.itemSize.text = cartItem.size
            binding.itemColor.text = cartItem.color

//            // Handle add and remove click listeners
//            binding.addImageView.setOnClickListener {
//                onIncreaseQuantity(cartItem)
//            }
//
//            binding.removeImageView.setOnClickListener {
//                onDecreaseQuantity(cartItem)
//            }
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
