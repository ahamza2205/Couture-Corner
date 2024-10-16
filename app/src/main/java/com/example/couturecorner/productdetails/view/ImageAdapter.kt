package com.example.couturecorner.productdetails.view

import android.util.Log


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.couturecorner.R

class ImageAdapter(
    private val imageUrls: List<String>,
    private val onImageClick: (String) -> Unit // Lambda to handle click events
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            Log.d("ImageAdapter", "Clicked Image URL: $imageUrl")
            onImageClick(imageUrl)
        }
    }

    override fun getItemCount(): Int = imageUrls.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImage)
    }
}

