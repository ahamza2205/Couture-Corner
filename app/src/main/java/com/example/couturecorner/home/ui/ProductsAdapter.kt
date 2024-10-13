package com.example.couturecorner.home.ui
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.couturecorner.R
import com.example.couturecorner.data.local.LocalListsData
import com.example.couturecorner.setting.viewmodel.CurrencyViewModel
import com.example.couturecorner.databinding.ProductItemBinding
import com.graphql.FilteredProductsQuery


class productdiifUtill():DiffUtil.ItemCallback<FilteredProductsQuery.Edge>() {
    override fun areItemsTheSame(
        oldItem: FilteredProductsQuery.Edge,
        newItem: FilteredProductsQuery.Edge
    ): Boolean {
        return oldItem.node?.id==newItem.node?.id
    }

    override fun areContentsTheSame(
        oldItem: FilteredProductsQuery.Edge,
        newItem: FilteredProductsQuery.Edge
    ): Boolean {
        return oldItem==newItem
    }

}

class ProductsAdapter(
    private val listener: OnItemClickListener,
) : ListAdapter<FilteredProductsQuery.Edge, ProductsAdapter.ProductsViewHolder>(productdiifUtill()) {

    val favList = mutableListOf<String>()

    class ProductsViewHolder(var binding: ProductItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ProductItemBinding.inflate(inflater, parent, false)
        return ProductsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = getItem(position).node
        val title = product?.title?.split("|")
        holder.binding.title.text = title?.get(1)

//        val selectedCurrency = viewModel.selectedCurrency.value ?: "EGP"
//        val originalPrice = product?.variants?.edges?.get(0)?.node?.price?.toDoubleOrNull() ?: 0.0
//
//        viewModel.convertCurrency("EGP", selectedCurrency, originalPrice) { convertedPrice ->
//            val priceWithSymbol = "${String.format("%.2f", convertedPrice ?: originalPrice)} ${getCurrencySymbol(selectedCurrency)}"
//            holder.binding.priceTextView.text = priceWithSymbol
//        }

     //   holder.binding.priceTextView.text= product?.variants?.edges?.get(0)?.node?.price

        holder.binding.priceTextView.text = holder.itemView.context.getString(
            R.string.price,
            product?.variants?.edges?.get(0)?.node?.price,
            listener.getcurrency()
        )
        holder.binding.ratingText.text=LocalListsData.productRatingsMap[product?.id]?.toString()

        holder.binding.favoriteAddsButton.isSelected = favList.contains(product?.id)

        Glide.with(holder.itemView.context)
            .load(product?.images?.edges?.get(0)?.node?.src)
            .into(holder.binding.ProductImageView)

        holder.binding.favoriteAddsButton.setOnClickListener {

            if (favList.contains(product?.id))
            {
                // delet method
                product?.id?.let { productId ->
                    listener.deleteFavorite(productId)
                }
                holder.binding.favoriteAddsButton.isSelected=false
            }
            else
            {
                product?.id?.let { productId ->
                    listener.onFavoriteClick(productId)
                }
                holder.binding.favoriteAddsButton.isSelected=true
            }

        }
        holder.itemView.setOnClickListener {
            listener.onItemClick(product)
        }
    }
//    fun getCurrencySymbol(currency: String): String {
//        return when (currency) {
//            "USD" -> "$"
//            "EUR" -> "€"
//            "EGP" -> "EGP"
//            "SAR" -> "SAR"
//            "AED" -> "AED"
//            else -> ""
//        }
//    }
    fun favListUpdate(favs: MutableList<String>) {
        favList.clear()
        favList.addAll(favs)

        val updatedList =   currentList.map { edge ->
            val product = edge.node
            if (product?.id in favList) {
                edge
            } else {
                edge
            }
        }
        submitList(updatedList)
    }

}