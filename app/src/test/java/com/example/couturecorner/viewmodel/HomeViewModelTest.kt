package com.example.couturecorner.viewmodel
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.example.couturecorner.home.viewmodel.HomeViewModel
import com.graphql.FilteredProductsQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class HomeViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repo: Irepo
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        viewModel = HomeViewModel(repo, mockk())  // Mock SharedPreferenceImp as needed
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test getFilteredProducts success`() = runTest {
        // Create a mock response for the ApolloResponse
        val mockResponse = mockk<ApolloResponse<FilteredProductsQuery.Data>> {
            every { hasErrors() } returns false
        }

        // Mock the repository to return ApiState.Loading first, followed by ApiState.Success
        coEvery { repo.getFilterdProducts(any()) } returns flowOf(mockResponse)


        // Call the function in the ViewModel
        viewModel.getFilterdProducts("clothing")

        // Assert that the state is as expected
        viewModel.products.test {


            // Then, it should emit Success state with the mockResponse
            val successState = awaitItem() as ApiState.Success
            assertEquals(mockResponse, successState.data)
        }

        // Verify the repository method was called with the correct argument
        coVerify { repo.getFilterdProducts("clothing") }
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun `test getFilteredProducts failure`() = runTest {
        // Create a mock response for the ApolloResponse
        val mockError = Error(
            message = "Failed to fetch products",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )
        val mockErrorResponse = ApolloResponse.Builder(
            operation = FilteredProductsQuery(),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the repository to return ApiState.Loading first, followed by ApiState.Error
        coEvery { repo.getFilterdProducts(any()) } returns flowOf(mockErrorResponse)


        // Call the function in the ViewModel
        viewModel.getFilterdProducts("clothing")

        // Assert that the state is as expected
        viewModel.products.test {

            // Then, it should emit Error state with the error message
            val errorState = awaitItem() as ApiState.Error
            assertEquals(mockError.message, errorState.message)
        }

        // Verify the repository method was called with the correct argument
        coVerify { repo.getFilterdProducts("clothing") }
    }
}