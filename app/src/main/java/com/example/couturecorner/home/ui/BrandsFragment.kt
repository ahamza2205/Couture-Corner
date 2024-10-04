package com.example.couturecorner.home.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.couturecorner.R


class BrandsFragment : Fragment() {

    private var brandName: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        brandName = arguments?.getString("brand")
        Log.d("BrandArgsTest", "$brandName: ")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brands, container, false)
    }


}