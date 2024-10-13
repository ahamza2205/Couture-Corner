package com.example.couturecorner.brand.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.couturecorner.R
import com.example.couturecorner.databinding.BrandItemBinding

class BrandsAdapter(
    val myListener:(String) -> Unit
):RecyclerView.Adapter<BrandsAdapter.BrandsViewHolder>() {
    var brands: ArrayList<String> = arrayListOf("vans", "palladium", "asics tiger", "puma",
        "supra", "adidas", "timberland", "nike", "dr martens", "converse", "herschel", "flex fit")

    val brandLogos: Map<String, Int> = mapOf("vans" to R.drawable.vans, "palladium" to R.drawable.palladium_logo,
        "asics tiger" to R.drawable.asics_tiger_logo, "puma" to R.drawable.puma, "supra" to R.drawable.supra_logo,
        "adidas" to R.drawable.adidas, "timberland" to R.drawable.timberland_logo, "nike" to R.drawable.nike,
        "dr martens" to R.drawable.dr_martins_logo, "converse" to R.drawable.converse_logo,
        "herschel" to R.drawable.herschel_logo ,"flex fit" to R.drawable.flexfit_logo)

    lateinit var binding: BrandItemBinding
    class BrandsViewHolder(var binding: BrandItemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsViewHolder {
        val inflater : LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding= BrandItemBinding.inflate(inflater, parent, false)
        return BrandsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    override fun onBindViewHolder(holder: BrandsViewHolder, position: Int) {
        val brand = brands[position]
        val logoResId = brandLogos[brand] ?: R.drawable.shoz10
        holder.binding.imageView.setImageResource(logoResId)
        holder.binding.imageView.setOnClickListener {
            myListener.invoke(brand)
        }
    }

}