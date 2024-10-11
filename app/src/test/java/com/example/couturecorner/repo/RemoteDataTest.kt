package com.example.couturecorner.repo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.remote.RemoteData
import com.graphql.AddFavoriteProductMutation
import com.graphql.GetFavoriteProductsQuery
import com.graphql.ProductQuery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RemoteDataTest {

    @Mock
    private lateinit var mockApolloClient: ApolloClient
    @Mock
    private lateinit var mockRemoteData: RemoteData

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockRemoteData = RemoteData(mockApolloClient)
    }


    // ---------------------------------- getProductDetails ----------------------------------
    @Test
    fun `test getProductDetails returns success`() = runTest {
        // Arrange: Mock response
        val mockProductData = ProductQuery.Data(
            product = ProductQuery.Product(
                description = "This is a sample product description",
                id = "123",
                productType = "Electronics",
                title = "Sample Product",
                totalInventory = 50,
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
                            displayName = "Sample Variant",
                            price = "19.99",
                            sku = "SKU123",
                            selectedOptions = listOf(ProductQuery.SelectedOption(name = "Color", value = "Red"))
                        ))
                    )
                )
            )
        )

        // Mocking the remote data to return the mock response
        `when`(mockRemoteData.getProductDetails("123")).thenReturn(flowOf(ApiState.Success(mockProductData)))

        // Act: Call the function
        val result = mockRemoteData.getProductDetails("123").first()

        // Assert: Verify the response data
        assertTrue(result is ApiState.Success)
        val data = (result as ApiState.Success).data
        assertNotNull(data?.product)
        assertEquals("123", data?.product?.id)
        assertEquals("Sample Product", data?.product?.title)
        assertEquals("This is a sample product description", data?.product?.description)
    }
    @Test
    fun `test getProductDetails returns error on failure`() = runTest {
        // Arrange: Mock an error response
        val errorResponse = ApiState.Error("Unknown Error")
        `when`(mockRemoteData.getProductDetails("123")).thenReturn(flowOf(errorResponse))

        // Act: Call the function
        val result = mockRemoteData.getProductDetails("123").first()

        // Assert: Verify that it returns an error state
        assertTrue(result is ApiState.Error)
        assertEquals("Unknown Error", (result as ApiState.Error).message)
    }


    @Test
    fun `test addProductToFavorites returns success`() = runTest {
        // Arrange
        `when`(mockRemoteData.addProductToFavorites("123", "456"))
            .thenReturn(Unit) // This ensures the function is mocked correctly

        // Act
        mockRemoteData.addProductToFavorites("123", "456")

        // Assert
        verify(mockRemoteData).addProductToFavorites("123", "456")
    }



    /*
        @Test
        fun `test getCurrentFavorites returns success`() = runTest {
            // Arrange
            val fakeResponse = FakeData.getFakeProductQueryResponse()
            `when`(mockRemoteData.getCurrentFavorites("123"))
                .thenReturn(flowOf(ApiState.Success(fakeResponse)))

            // Act
            val result = mockRemoteData.getCurrentFavorites("123").first()

            // Assert
            assertTrue(result is ApiState.Success<*>)
            val data = (result as ApiState.Success<*>).data
            assertNotNull(data?.customer?.metafields)
            assertEquals("[\"123\"]", data?.customer?.metafields?.edges?.first()?.node?.value)
        }
    */


/*    @Test
    fun `test addProductToFavorites returns error`() = runTest {
        // Arrange
        val errorResponse = ApiState.Error("Failed to add product to favorites")
        `when`(mockRemoteData.addProductToFavorites("123", "456"))
            .thenReturn(flowOf(errorResponse))

        // Act
        val result = mockRemoteData.addProductToFavorites("123", "456").first()

        // Assert
        assertTrue(result is ApiState.Error)
        assertEquals("Failed to add product to favorites", (result as ApiState.Error).message)
    }

    @Test
    fun `test getCurrentFavorites returns error`() = runTest {
        // Arrange
        val errorResponse = ApiState.Error("Failed to retrieve favorite products")
        `when`(mockRemoteData.getCurrentFavorites("123"))
            .thenReturn(flowOf(errorResponse))

        // Act
        val result = mockRemoteData.getCurrentFavorites("123")?.first()

        // Assert
        assertTrue(result is ApiState.Error)
        assertEquals("Failed to retrieve favorite products", (result as ApiState.Error).message)
    }*/
}


