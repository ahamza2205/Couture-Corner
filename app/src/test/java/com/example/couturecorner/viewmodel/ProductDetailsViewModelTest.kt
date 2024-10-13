//package com.example.couturecorner.viewmodel
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.example.couturecorner.data.model.ApiState
//import com.example.couturecorner.data.repository.Repo
//import com.example.couturecorner.productdetails.viewmodel.ProductDetailsViewModel
//import com.graphql.ProductQuery
//import io.mockk.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.*
//import org.junit.After
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//
//@ExperimentalCoroutinesApi
//class ProductDetailsViewModelTest {
//    // Rule to make LiveData execute immediately
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    // Set the main dispatcher for coroutines
//    private val testDispatcher = UnconfinedTestDispatcher()
//
//    // Mocked dependencies
//    private lateinit var repo: Repo
//    private lateinit var viewModel: ProductDetailsViewModel
//
//    @Before
//    fun setUp() {
//        // Set the dispatcher for coroutines
//        Dispatchers.setMain(testDispatcher)
//
//        // Initialize mock objects
//        repo = mockk()
//        viewModel = ProductDetailsViewModel(repo)
//    }
//
//    @After
//    fun tearDown() {
//        // Reset the dispatcher to avoid any side effects
//        Dispatchers.resetMain()
//    }
//
//    // -------------------------------------- Test Success Case --------------------------------------
//    @Test
//    fun `test getProductDetails success`() = runTest {
//        // Mock ProductQuery.Data to simulate success response
//        val mockProductData = mockk<ProductQuery.Data>()
//
//        // Mock the repository to return flow with ApiState.Success
//        coEvery { repo.getProductDetails(any()) } returns flowOf(ApiState.Success(mockProductData))
//
//        // Call the function in the ViewModel
//        viewModel.getProductDetails("product123")
//
//        // Collect the flow and assert that it's in the Success state
//        val state = viewModel.productDetails.value
//        assert(state is ApiState.Success)
//        assertEquals(mockProductData, (state as ApiState.Success).data)
//
//        // Verify that the Repo's method was called
//        coVerify { repo.getProductDetails("product123") }
//    }
//
//    // -------------------------------------- Test Loading Case --------------------------------------
//    @Test
//    fun `test getProductDetails loading`() = runTest {
//        // Mock the repository to simulate loading state
//        coEvery { repo.getProductDetails(any()) } returns flowOf(ApiState.Loading)
//
//        // Call the function in the ViewModel
//        viewModel.getProductDetails("product123")
//
//        // Assert that the initial state is Loading
//        val state = viewModel.productDetails.value
//        assert(state is ApiState.Loading)
//
//        // Verify that the Repo's method was called
//        coVerify { repo.getProductDetails("product123") }
//    }
//    // -------------------------------------- Test Error Case --------------------------------------
//    @Test
//    fun `test getProductDetails error`() = runTest {
//        // Simulate an error state
//        val errorMessage = "Error fetching product details"
//        coEvery { repo.getProductDetails(any()) } returns flowOf(ApiState.Error(errorMessage))
//
//        // Call the function in the ViewModel
//        viewModel.getProductDetails("product123")
//
//        // Collect the flow and assert that it's in the Error state
//        val state = viewModel.productDetails.value
//        assert(state is ApiState.Error)
//        assertEquals(errorMessage, (state as ApiState.Error).message)
//
//        // Verify that the Repo's method was called
//        coVerify { repo.getProductDetails("product123") }
//    }
//}