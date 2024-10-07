package com.example.couturecorner.home.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.couturecorner.R
import com.example.couturecorner.databinding.CuponItemBinding
import com.graphql.GetCuponCodesQuery


class CuponDiffUtill: DiffUtil.ItemCallback<GetCuponCodesQuery.Node>() {
    override fun areItemsTheSame(
        oldItem: GetCuponCodesQuery.Node,
        newItem: GetCuponCodesQuery.Node
    ): Boolean {
       return oldItem.id==newItem.id
    }

    override fun areContentsTheSame(
        oldItem: GetCuponCodesQuery.Node,
        newItem: GetCuponCodesQuery.Node
    ): Boolean {
       return oldItem==newItem
    }


}

class CuponAdapter:ListAdapter<GetCuponCodesQuery.Node, CuponAdapter.CuponViewHolder>(CuponDiffUtill()) {

    val cuponLogo: Map<String, Int> = mapOf("5% off 17 collections" to R.drawable.five_per, "10% off 17 collections" to R.drawable.ten,
        "15% off 17 collections" to R.drawable.fifteen_per, "20% off 17 collections" to R.drawable.twentee
        ,"25% off 17 collections" to R.drawable.twentee_five_per)

    lateinit var binding: CuponItemBinding
    class CuponViewHolder(var binding: CuponItemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuponViewHolder {

        val inflater : LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding= CuponItemBinding.inflate(inflater, parent, false)
        return CuponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CuponViewHolder, position: Int) {
        val cupon = getItem(position)
        cupon.codeDiscount.onDiscountCodeBasic?.summary
        val logoResId = cuponLogo[cupon.codeDiscount.onDiscountCodeBasic?.summary] ?: R.drawable.five_per
        holder.binding.backgroundImage.setImageResource(logoResId)
    }


}