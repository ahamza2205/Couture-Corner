package com.example.couturecorner.productdetails.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.data.local.Review
import com.example.couturecorner.databinding.ReviewItemBinding

class ReviewAdapter(
    val reviewList:MutableList<Review>
):RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    lateinit var binding:ReviewItemBinding
    class ReviewViewHolder(val binding:ReviewItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        binding=ReviewItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ReviewViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return reviewList.size
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        binding.reviewerNameTextView.text=review.reviewerName
        binding.reviewTextView.text=review.text
        binding.ratingBar.rating=review.rating
    }

}