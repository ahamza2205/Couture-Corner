package com.example.couturecorner.category.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.R
import com.example.couturecorner.databinding.CategoryItemBinding

class CategoryAdapter(
   val myListenner:(String)->Unit
): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    val categoryList = listOf("women", "men", "kid","sale")

    val categoryLogos: Map<String, Int> = mapOf("women" to R.drawable.baseline_woman_24, "men" to R.drawable.baseline_man_24,
        "kid" to R.drawable.baseline_child_friendly_24, "sale" to R.drawable.baseline_discount_24)

    lateinit var binding: CategoryItemBinding

    class CategoryViewHolder(var binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater : LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding= CategoryItemBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
       val category = categoryList[position]
        val logoResId = categoryLogos[category] ?: R.drawable.baseline_woman_24
        holder.binding.circularImageView.setImageResource(logoResId)
        holder.binding.circularImageView.setOnClickListener {
            myListenner.invoke(category)
        }
    }
}