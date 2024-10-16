package com.example.couturecorner.viewmodel


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.example.couturecorner.order.viewModel.OrdersViewModel
import com.graphql.OrderByIdQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class OrdersViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repo: Irepo
    private lateinit var viewModel: OrdersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        viewModel = OrdersViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test getOrderById success`() = runTest {
        // Create a mock response for ApolloResponse
        val mockResponse = mockk<ApolloResponse<OrderByIdQuery.Data>> {
            every { hasErrors() } returns false
        }

        // Mock the repository to return ApiState.Success
        coEvery { repo.getOrderById(any()) } returns flowOf(mockResponse)

        // Call the function in the ViewModel
        viewModel.getOrderById("order_id")

        // Assert that the state is as expected
        viewModel.ordersId.test {
            val successState = awaitItem() as ApiState.Success
            assertEquals(mockResponse, successState.data)
        }

        // Verify the repository method was called with the correct argument
        coVerify { repo.getOrderById("order_id") }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test getOrderById failure`() = runTest {
        // Create a mock error response for ApolloResponse
        val mockError = Error(
            message = "Failed to fetch order",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )
        val mockErrorResponse = ApolloResponse.Builder(
            operation = OrderByIdQuery("order_id"),
            data = null,
            requestUuid = java.util.UUID.randomUUID()
        ).errors(listOf(mockError)).build()

        // Mock the repository to return an error response
        coEvery { repo.getOrderById(any()) } returns flowOf(mockErrorResponse)

        // Call the function in the ViewModel
        viewModel.getOrderById("order_id")

        // Assert that the state is as expected
        viewModel.ordersId.test {
            val errorState = awaitItem() as ApiState.Error
            assertEquals(mockError.message, errorState.message)
        }

        // Verify the repository method was called with the correct argument
        coVerify { repo.getOrderById("order_id") }
    }
}