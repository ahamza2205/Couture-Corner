package com.example.couturecorner.order.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.couturecorner.R
import com.example.couturecorner.databinding.OrderDetailsItemBinding
import com.example.couturecorner.databinding.OrderItemBinding
import com.graphql.OrderByIdQuery
import com.graphql.adapter.OrderByIdQuery_ResponseAdapter
import com.graphql.type.LineItemConnection

class OrderItemDiffUtill : DiffUtil.ItemCallback<OrderByIdQuery.Edge>() {
    override fun areItemsTheSame(
        oldItem: OrderByIdQuery.Edge,
        newItem: OrderByIdQuery.Edge
    ): Boolean {
       return oldItem.node?.name == newItem.node?.name
    }

    override fun areContentsTheSame(
        oldItem: OrderByIdQuery.Edge,
        newItem: OrderByIdQuery.Edge
    ): Boolean {
      return oldItem==newItem
    }


}

class OrderItemAdapter:ListAdapter<OrderByIdQuery.Edge,OrderItemAdapter.OrderItemViewHolder>(OrderItemDiffUtill()) {
    lateinit var binding: OrderDetailsItemBinding
    class OrderItemViewHolder(var binding: OrderDetailsItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val inflater : LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding= OrderDetailsItemBinding.inflate(inflater, parent, false)
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val order = getItem(position).node
      holder.binding.productNameTextView.text=order?.name
        holder.binding.productQuantityTextView.text=order?.quantity.toString()
        binding.productPriceTextView.text=holder.itemView.context.getString(
            R.string.priceOrder,
            order?.originalUnitPriceSet?.shopMoney?.amount,
            order?.originalUnitPriceSet?.shopMoney?.currencyCode)

        Glide.with(holder.itemView.context)
            .load(order?.image?.src)
            .into(holder.binding.productImageView)
    }
}