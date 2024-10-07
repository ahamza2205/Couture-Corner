package com.example.couturecorner.brand.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.couturecorner.databinding.ProductItemBinding
import com.example.couturecorner.home.ui.ProductsAdapter.ProductsViewHolder
import com.graphql.FilteredProductsQuery

class ProductBrandDiffUtill():DiffUtil.ItemCallback<FilteredProductsQuery.Edge>()
{
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
class ProductBrandAdapter():ListAdapter<FilteredProductsQuery.Edge,ProductBrandAdapter.ProductBrandViewHolder>(ProductBrandDiffUtill()){

    lateinit var binding: ProductItemBinding
    class ProductBrandViewHolder(var binding: ProductItemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductBrandViewHolder {
        val inflater : LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding= ProductItemBinding.inflate(inflater, parent, false)
        return ProductBrandViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductBrandViewHolder, position: Int) {

        val product=getItem(position).node
        val title=product?.title?.split("|")
        holder.binding.title.text=title?.get(1)
//      holder.binding.title.text=product?.title
        holder.binding.priceTextView.text=product?.variants?.edges?.get(0)?.node?.price

        Glide.with(holder.itemView.context)
            .load(product?.images?.edges?.get(0)?.node?.src)
            .into(holder.binding.ProductImageView)

    }
}