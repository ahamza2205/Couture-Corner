package com.example.couturecorner.favorite.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.couturecorner.setting.viewmodel.CurrencyViewModel
import com.example.couturecorner.R
import com.example.couturecorner.databinding.ProductItemBinding
import com.graphql.ProductQuery

class FavoriteProductsAdapter(
    private val listener: OnFavoriteItemClickListener,
//    private val viewModel: CurrencyViewModel

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
//        val selectedCurrency = viewModel.selectedCurrency.value ?: "EGP"
//        val originalPrice = product?.variants?.edges?.get(0)?.node?.price?.toDoubleOrNull() ?: 0.0
//        viewModel.convertCurrency("EGP", selectedCurrency, originalPrice) { convertedPrice ->
//            val priceWithSymbol = "${String.format("%.2f", convertedPrice ?: originalPrice)} ${getCurrencySymbol(selectedCurrency)}"
//            holder.binding.priceTextView.text = priceWithSymbol
//        }
        holder.binding.priceTextView.text=holder.itemView.context.getString(
            R.string.price,
            product.variants?.edges?.get(0)?.node?.price,
            listener.currencySymbol()
        )


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
        holder.binding.favoriteAddsButton.setOnClickListener {
            listener.onFavoriteClick(product.id) // Trigger the remove favorite functionality
        }
    }
    fun getCurrencySymbol(currency: String): String {
        return when (currency) {
            "USD" -> "$"
            "EUR" -> "€"
            "EGP" -> "EGP"
            "SAR" -> "SAR"
            "AED" -> "AED"
            else -> ""
        }
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


