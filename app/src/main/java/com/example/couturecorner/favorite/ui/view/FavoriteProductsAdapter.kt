package com.example.couturecorner.favorite.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.couturecorner.databinding.ProductItemBinding
import com.graphql.ProductQuery

class FavoriteProductsAdapter(
    private val listener: OnFavoriteItemClickListener
) : RecyclerView.Adapter<FavoriteProductsAdapter.FavoriteProductViewHolder>() {

    private val productList = mutableListOf<ProductQuery.Product>() // This should hold ProductQuery.Product objects



    class FavoriteProductViewHolder(val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteProductViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding = ProductItemBinding.inflate(inflater, parent, false)
        return FavoriteProductViewHolder(binding)
    }
    override fun onBindViewHolder(holder: FavoriteProductViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.title.text = product.title
        holder.binding.priceTextView.text = product.variants?.edges?.get(0)?.node?.price

        holder.binding.favoriteAddsButton.isSelected=checkIsFavorite(product.id)

      holder.binding.favoriteAddsButton.setOnClickListener {

              // delet method

              listener.onFavoriteClick(product.id)

              holder.binding.favoriteAddsButton.isSelected=false
         
      }


        Glide.with(holder.itemView.context)
            .load(product.images?.edges?.get(0)?.node?.src)
            .into(holder.binding.ProductImageView)
        // Set click listener for the item
        holder.itemView.setOnClickListener {
            listener.onItemClick(product)
        }
        // Set click listener for the favorite button

    }
    override fun getItemCount(): Int = productList.size
    fun submitList(products: List<ProductQuery.Product>) {
        productList.clear()
        productList.addAll(products)
        notifyDataSetChanged()
    }

    fun checkIsFavorite(productId: String): Boolean {
        return productList.any { it.id == productId }
    }
}


