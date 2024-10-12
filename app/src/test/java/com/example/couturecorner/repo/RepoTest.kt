package com.example.couturecorner.repo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.remote.CurrencyApiService
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.data.remote.RemoteData
import com.example.couturecorner.data.repository.Repo
import com.graphql.AddFavoriteProductMutation
import com.graphql.GetCustomerByIdQuery
import com.graphql.GetFavoriteProductsQuery
import com.graphql.ProductQuery
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RepoTest {

    // Mock the dependencies
    private lateinit var remoteData: IremoteData
    private lateinit var sharedPreference: SharedPreference
    private lateinit var apiService: CurrencyApiService
    private lateinit var repo: Repo

    @Before
    fun setUp() {
        // Initialize mocks
        remoteData = mockk()
        sharedPreference = mockk()
        apiService = mockk()

        // Initialize Repo with the mocked dependencies
        repo = Repo(remoteData, sharedPreference, apiService)
    }

    // ------------------------------- test registerUser -------------------------------
    @Test
    fun `test registerUser successfully`() = runTest {
        // Mock the behavior of remoteData.registerUser
        coEvery { remoteData.registerUser(any(), any(), any(), any(), any()) } returns "mockUserId"

        // Call the function in Repo
        val result = repo.registerUser("test@example.com", "password", "FirstName", "LastName", "123456", "token")

        // Verify the behavior and result
        coVerify { remoteData.registerUser("test@example.com", "password", "FirstName", "LastName", "123456") }
        assertEquals("mockUserId", result)
    }
    // ---------------------------------------- registerUser with missing fields ----------------------------------------
    @Test(expected = IllegalArgumentException::class)
    fun `test registerUser with missing fields`() = runTest {
        // Mock the behavior of remoteData.registerUser to throw IllegalArgumentException when called with nulls
        coEvery { remoteData.registerUser(null, null, any(), null, any(), null) } throws IllegalArgumentException()

        // Call the function in Repo
        repo.registerUser(null, null, "FirstName", null, "123456", null)
    }
    // ------------------------------------------ test getCustomerByEmail ------------------------------------------
    @Test
    fun `test getCustomerByEmail successfully`() = runTest {
        // Mock the behavior of remoteData.getCustomerByEmail
        coEvery { remoteData.getCustomerByEmail(any()) } returns "mockCustomerId"

        // Call the function in Repo
        val result = repo.getCustomerByEmail("test@example.com")

        // Verify the behavior and result
        coVerify { remoteData.getCustomerByEmail("test@example.com") }
        assertEquals("mockCustomerId", result)
    }

    // ------------------------------------------ test getCustomerById ------------------------------------------
    @Test
    fun `test getCustomerById returns customer data successfully`() = runTest {
        // Arrange: Prepare mock response
        val mockCustomer = GetCustomerByIdQuery.Customer(
            id = "customer123",
            displayName = "John Doe",
            email = "john.doe@example.com",
            firstName = "John",
            lastName = "Doe",
            phone = "1234567890",
            createdAt = "2023-10-10T00:00:00Z",
            updatedAt = "2023-10-12T00:00:00Z",
            defaultAddress = GetCustomerByIdQuery.DefaultAddress(
                address1 = "123 Main St",
                address2 = "Apt 4B",
                city = "New York",
                phone = "1234567890"
            )
        )

        coEvery { remoteData.getCustomerById("customer123") } returns mockCustomer

        // Act: Call the function
        val result = repo.getCustomerById("customer123")

        // Assert: Check the result
        assertNotNull(result)
        assertEquals("John", result?.firstName)
        assertEquals("Doe", result?.lastName)
        assertEquals("john.doe@example.com", result?.email)

        coVerify { remoteData.getCustomerById("customer123") }
    }

    // ------------------------------------------ test getCustomerById with error ------------------------------------------
    @Test
    fun `test getCustomerById throws exception on error`() = runTest {
        // Arrange: Prepare mock exception
        coEvery { remoteData.getCustomerById("customer123") } throws Exception("Error fetching customer")

        // Act & Assert: Expect the exception
        val exception = assertThrows(Exception::class.java) {
            runBlocking { repo.getCustomerById("customer123") }
        }

        // Verify the exception message
        assertEquals("Error fetching customer", exception.message)

        coVerify { remoteData.getCustomerById("customer123") }
    }
 // ------------------------------------------ test getProductDetails ------------------------------------------
    @Test
    fun `test getProductDetails returns success`() = runTest {
        // Arrange: Mock data for the product query
        val mockProductData = ProductQuery.Data(
            product = ProductQuery.Product(
                description = "A great product",
                id = "123",
                productType = "Shoes",
                title = "Sneakers",
                totalInventory = 100,
                vendor = "Sample Vendor",
                images = ProductQuery.Images(
                    edges = listOf(
                        ProductQuery.Edge(node = ProductQuery.Node(src = "https://example.com/sample.jpg"))
                    )
                ),
                variants = ProductQuery.Variants(
                    edges = listOf(
                        ProductQuery.Edge1(node = ProductQuery.Node1(
                            id = "123-variant",
                            displayName = "Size 10",
                            price = "19.99",
                            sku = "SKU123",
                            selectedOptions = listOf(ProductQuery.SelectedOption(name = "Color", value = "Red"))
                        ))
                    )
                )
            )
        )


        // Mock the remoteData call
        coEvery { remoteData.getProductDetails("123") } returns flow {
            emit(ApiState.Success(mockProductData))
        }

        // Act: Call the function and collect the result
        repo.getProductDetails("123").collect { result ->
            when (result) {
                is ApiState.Success -> {
                    val product = result.data?.product
                    assertEquals("Sneakers", product?.title)
                    assertEquals("Shoes", product?.productType)
                    assertEquals("A great product", product?.description)
                    assertEquals(100, product?.totalInventory)
                    assertEquals("https://example.com/sample.jpg", product?.images?.edges?.get(0)?.node?.src)
                    assertEquals("Size 10", product?.variants?.edges?.get(0)?.node?.displayName)
                }
                is ApiState.Error -> {
                    throw AssertionError("Expected success but got error")
                }
                is ApiState.Loading -> {
                    // Handle loading state if necessary
                }
            }
        }

        // Verify the call was made correctly
        coVerify { remoteData.getProductDetails("123") }
    }

// ------------------------------------------ test getProductDetails with error ------------------------------------------
    @Test
    fun `test getProductDetails throws exception on error`() = runTest {
        // Arrange: Mock the exception
        coEvery { remoteData.getProductDetails("123") } throws Exception("Error fetching product")

        // Act & Assert: Verify that the exception is thrown
        val exception = assertThrows(Exception::class.java) {
            // Collect the Flow in the coroutine context
            runBlocking {
                repo.getProductDetails("123").collect { }            }
        }

        assertEquals("Error fetching product", exception.message)

        // Verify the call
        coVerify { remoteData.getProductDetails("123") }
    }
    // -------------------------------------- Test addProductToFavorites --------------------------------------
    @Test
    fun `test addProductToFavorites successfully`() = runTest {
        // Mock RemoteData addProductToFavorites call
        coEvery { remoteData.addProductToFavorites(any(), any()) } just Runs

        // Call the function
        repo.addProductToFavorites("customer123", "product123")

        // Verify the behavior
        coVerify { remoteData.addProductToFavorites("customer123", "product123") }
    }
    @Test
    fun `test addProductToFavorites fails with exception`() = runTest {
        // Mock RemoteData to throw exception
        coEvery { remoteData.addProductToFavorites(any(), any()) } throws Exception("Error adding product")

        // Check that the exception is thrown when calling the function
        val exception = assertThrows(Exception::class.java) {
            runBlocking { repo.addProductToFavorites("customer123", "product123") }
        }

        assertEquals("Error adding product", exception.message)

        coVerify { remoteData.addProductToFavorites("customer123", "product123") }
    }

    // -------------------------------------- Test getCurrentFavorites --------------------------------------
    @Test
    fun `test getCurrentFavorites returns favorites list`() = runTest {
        // Mock data for current favorites
        val mockFavorites = listOf("product123", "product456")

        // Mock RemoteData to return the list
        coEvery { remoteData.getCurrentFavorites(any()) } returns mockFavorites

        // Call the function
        val result = repo.getCurrentFavorites("customer123")

        // Verify the result and behavior
        assertEquals(mockFavorites, result)
        coVerify { remoteData.getCurrentFavorites("customer123") }
    }
    @Test
    fun `test getCurrentFavorites returns empty list`() = runTest {
        // Mock empty list for favorites
        coEvery { remoteData.getCurrentFavorites(any()) } returns emptyList()

        // Call the function
        val result = repo.getCurrentFavorites("customer123")

        // Verify the result
        assertEquals(emptyList<String>(), result)
        coVerify { remoteData.getCurrentFavorites("customer123") }
    }

    // -------------------------------------- Test removeProductFromFavorites --------------------------------------
    @Test
    fun `test removeProductFromFavorites successfully`() = runTest {
        // Mock RemoteData removeProductFromFavorites
        coEvery { remoteData.removeProductFromFavorites(any(), any()) } just Runs

        // Call the function
        repo.removeProductFromFavorites("customer123", "product123")

        // Verify the behavior
        coVerify { remoteData.removeProductFromFavorites("customer123", "product123") }
    }
    @Test
    fun `test removeProductFromFavorites fails with exception`() = runTest {
        // Mock RemoteData to throw exception
        coEvery { remoteData.removeProductFromFavorites(any(), any()) } throws Exception("Error removing product")

        // Check that the exception is thrown when calling the function
        val exception = assertThrows(Exception::class.java) {
            runBlocking { repo.removeProductFromFavorites("customer123", "product123") }
        }

        assertEquals("Error removing product", exception.message)

        coVerify { remoteData.removeProductFromFavorites("customer123", "product123") }
    }
}