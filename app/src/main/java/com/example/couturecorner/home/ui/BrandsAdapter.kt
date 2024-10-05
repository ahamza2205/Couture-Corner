package com.example.couturecorner.home.ui

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
    var brands: ArrayList<String> = arrayListOf("VANS", "PALLADIUM", "ASICS TIGER", "PUMA",
        "SUPRA", "ADIDAS", "TIMBERLAND", "NIKE", "DR MARTENS", "CONVERSE", "HERSCHEL", "FLEX FIT")

    val brandLogos: Map<String, Int> = mapOf("VANS" to R.drawable.vans_logo, "PALLADIUM" to R.drawable.palladium_logo,
        "ASICS TIGER" to R.drawable.asics_tiger_logo, "PUMA" to R.drawable.puma_logo, "SUPRA" to R.drawable.supra_logo,
        "ADIDAS" to R.drawable.adidas_logo, "TIMBERLAND" to R.drawable.timberland_logo, "NIKE" to R.drawable.nike,
        "DR MARTENS" to R.drawable.dr_martins_logo, "CONVERSE" to R.drawable.converse_logo,
        "HERSCHEL" to R.drawable.herschel_logo ,"FLEX FIT" to R.drawable.flexfit_logo)

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
        holder.binding.circularImageView.setImageResource(logoResId)
        holder.binding.circularImageView.setOnClickListener {
            myListener.invoke(brand)
        }
    }

}