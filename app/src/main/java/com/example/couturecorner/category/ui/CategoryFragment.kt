package com.example.couturecorner.category.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.couturecorner.R
import com.example.couturecorner.brand.ui.ProductBrandAdapter
import com.example.couturecorner.category.viewModel.CategoryViewModel
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.databinding.FragmentCategoryBinding
import com.example.couturecorner.home.ui.OnItemClickListener
import com.example.couturecorner.home.ui.ProductsAdapter
import com.example.couturecorner.home.viewmodel.MainViewModel
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryFragment : Fragment(), OnItemClickListener {

    private var category: String? = null
    lateinit var binding:FragmentCategoryBinding

    val viewModel: CategoryViewModel by viewModels()
    val sharedViewModel: MainViewModel by activityViewModels()

    lateinit var categoryAdapter:ProductsAdapter

    val categoryLogos: Map<String, Int> = mapOf("women" to R.drawable.woman, "men" to R.drawable.men,
        "kid" to R.drawable.shopping, "sale" to R.drawable.sale)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        category = arguments?.getString("category")
        Log.d("CategoryArgsTest", "$category: ")
       binding= FragmentCategoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        categoryAdapter= ProductsAdapter(this)
        binding.productsRecycel.adapter=categoryAdapter

        viewModel.getFilterdProducts(category)

        val logoResId = categoryLogos[category] ?: R.drawable.shoz10
        binding.categoryImageView.setImageResource(logoResId)

        lifecycleScope.launch {
            viewModel.productsCategory.collect{
                when(it){
                    is ApiState.Loading->showLoading(true)
                    is ApiState.Success->{
                        val products = it.data?.data?.products?.edges

                        showLoading(false)
                        categoryAdapter.submitList(products)
//                        products?.forEach { edge ->
//                            val productTag = edge?.node?.tags
//                           // productTag?.forEach { tag -> Log.d("AmrCategoryApollo", "tag: ${tag}, Description: ") }
//                            Log.d("AmrCategoryApollo", "Product: ${edge?.node?.title}, Description: ")
//                        }
                    }
                    is ApiState.Error->{
                        showLoading(false)
                        Log.d("AmrApollo", "${it.message} ")
                    }
                }
            }
        }

        sharedViewModel.getFavList()

        lifecycleScope.launch {
            sharedViewModel.favIdsList.collect{
                if(it.isNotEmpty()){
                    categoryAdapter.favListUpdate(it.toMutableList())
                }
            }
        }

    }
    override fun onItemClick(product: FilteredProductsQuery.Node?) {
        // Handle item click, e.g., navigate to a detailed product page
        Log.d("BrandFragment", "Clicked on product: ${product?.title}")
    }



    override fun onFavoriteClick(productId: String) {
        sharedViewModel.addProductToFavorites(productId)
        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
        Log.d("BrandFragment", "Favorited product ID: $productId")
    }
    fun showLoading(isLoading:Boolean)
    {
        if (isLoading)
        {
            binding.progressBar.visibility=View.VISIBLE
            binding.productsRecycel.visibility=View.GONE
        }
        else
        {
            binding.progressBar.visibility=View.GONE
            binding.productsRecycel.visibility=View.VISIBLE
        }
    }

//    override fun isFavorite(productId: String): Boolean {
//        TODO("Not yet implemented")
//    }
}