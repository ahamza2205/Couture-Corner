package com.example.couturecorner.category.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.couturecorner.R


class CategoryFragment : Fragment() {

    private var category: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        category = arguments?.getString("category")
        Log.d("CategoryArgsTest", "$category: ")
        return inflater.inflate(R.layout.fragment_category, container, false)
    }
}