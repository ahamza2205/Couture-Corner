package com.example.couturecorner.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.couturecorner.databinding.ProductItemBinding
import com.graphql.HomeProductsQuery

class ProductDiffUtil : DiffUtil.ItemCallback<HomeProductsQuery.Edge>() {
    override fun areItemsTheSame(
        oldItem: HomeProductsQuery.Edge,
        newItem: HomeProductsQuery.Edge
    ): Boolean {
        return oldItem.node?.id == newItem.node?.id
    }

    override fun areContentsTheSame(
        oldItem: HomeProductsQuery.Edge,
        newItem: HomeProductsQuery.Edge
    ): Boolean {
        return oldItem == newItem
    }
}

class ProductsAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<HomeProductsQuery.Edge, ProductsAdapter.ProductsViewHolder>(ProductDiffUtil()) {

    class ProductsViewHolder(var binding: ProductItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding = ProductItemBinding.inflate(inflater, parent, false)
        return ProductsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = getItem(position).node
        val title = product?.title?.split("|")
        holder.binding.title.text = title?.get(1)
        holder.binding.priceTextView.text = product?.variants?.edges?.get(0)?.node?.price

        Glide.with(holder.itemView.context)
            .load(product?.images?.edges?.get(0)?.node?.src)
            .into(holder.binding.ProductImageView)

        holder.itemView.setOnClickListener {
            listener.onItemClick(product)
        }
    }
}
