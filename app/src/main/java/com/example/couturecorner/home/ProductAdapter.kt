package com.example.couturecorner.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.couturecorner.R
import com.graphql.GetProductsQuery

class ProductAdapter(private val productList: List<GetProductsQuery.Edge?>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private var filteredList = productList.toMutableList()
    fun filter(query: String) {
        filteredList = productList.filter {
            it?.node?.title?.contains(query, ignoreCase = true) == true
        }.toMutableList()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = filteredList[position]?.node // Get the Node from the Edge
        holder.bind(product)
    }
    override fun getItemCount() = filteredList.size
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.productTitle)
        private val image: ImageView = itemView.findViewById(R.id.productImage)
        fun bind(product: GetProductsQuery.Node?) {
            title.text = product?.title
            // Use Glide to load the image
            Glide.with(itemView.context)
                .load(product?.images?.edges?.get(0)?.node?.src)
                .into(image)
        }
    }
}
