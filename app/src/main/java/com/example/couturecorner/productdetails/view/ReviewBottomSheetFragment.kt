package com.example.couturecorner.productdetails.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.couturecorner.data.local.LocalListsData
import com.example.couturecorner.databinding.FragmentReviewButtonSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ReviewBottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var binding:FragmentReviewButtonSheetBinding

    lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentReviewButtonSheetBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewAdapter= ReviewAdapter(LocalListsData.sampleReviews)
        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.reviewsRecyclerView.adapter = reviewAdapter

    }
}