package com.example.couturecorner.repo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.exception.ApolloException
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.remote.CurrencyApiService
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.data.remote.RemoteData
import com.example.couturecorner.data.repository.Repo
import com.graphql.AddFavoriteProductMutation
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetCustomerByIdQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.GetFavoriteProductsQuery
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.OrderByIdQuery
import com.graphql.ProductQuery
import com.graphql.UpdateDraftOrderMetafieldsMutation
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.UUID

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
    fun test_registerUser_successfully() = runTest {
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
    fun test_registerUser_withMissingFields() = runTest {
        // Mock the behavior of remoteData.registerUser to throw IllegalArgumentException when called with nulls
        coEvery { remoteData.registerUser(null, null, any(), null, any(), null) } throws IllegalArgumentException()

        // Call the function in Repo
        repo.registerUser(null, null, "FirstName", null, "123456", null)
    }
    // ------------------------------------------ test getCustomerByEmail ------------------------------------------
    @Test
    fun test_getCustomerByEmail_successfully() = runTest {
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
    fun test_getCustomerById_returnsCustomerDataSuccessfully() = runTest {
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
            ),
            // Add an empty list or a mock list of addresses
            addresses = emptyList() // or you can provide a list of Address objects
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
    fun test_getCustomerById_throws_exception_on_error() = runTest {
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
    fun test_getProductDetails_returns_success() = runTest {
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
    fun test_getProductDetails_throws_exception_on_error() = runTest {
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
    fun test_addProductToFavorites_successfully() = runTest {
        // Mock RemoteData addProductToFavorites call
        coEvery { remoteData.addProductToFavorites(any(), any()) } just Runs

        // Call the function
        repo.addProductToFavorites("customer123", "product123")

        // Verify the behavior
        coVerify { remoteData.addProductToFavorites("customer123", "product123") }
    }
    @Test
    fun test_addProductToFavorites_fails_with_exception() = runTest {
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
    fun test_getCurrentFavorites_returns_favorites_list() = runTest {
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
    fun test_getCurrentFavorites_returns_empty_list() = runTest {
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
    fun test_removeProductFromFavorites_successfully() = runTest {
        // Mock RemoteData removeProductFromFavorites
        coEvery { remoteData.removeProductFromFavorites(any(), any()) } just Runs

        // Call the function
        repo.removeProductFromFavorites("customer123", "product123")

        // Verify the behavior
        coVerify { remoteData.removeProductFromFavorites("customer123", "product123") }
    }
    @Test
    fun test_removeProductFromFavorites_fails_with_exception() = runTest {
        // Mock RemoteData to throw exception
        coEvery { remoteData.removeProductFromFavorites(any(), any()) } throws Exception("Error removing product")

        // Check that the exception is thrown when calling the function
        val exception = assertThrows(Exception::class.java) {
            runBlocking { repo.removeProductFromFavorites("customer123", "product123") }
        }

        assertEquals("Error removing product", exception.message)

        coVerify { remoteData.removeProductFromFavorites("customer123", "product123") }
    }

    // -------------------------------------- Test getProducts --------------------------------------
    @Test
    fun test_getProducts_returns_success() = runTest {
        // Arrange: Mock data for products query
        val mockProducts =  GetProductsQuery.Data(
            GetProductsQuery.Products(
                listOf(
                    GetProductsQuery.Edge(
                        GetProductsQuery.Node(
                            id = "123",
                            title = "Sneakers",
                            description = "A great product",
                            handle = "details about the product",
                            images = GetProductsQuery.Images(
                                listOf(GetProductsQuery.Edge1(
                                    GetProductsQuery.Node1(
                                        src = "https://example.com/sample.jpg"
                                    )
                                ))
                            ),
                            variants = GetProductsQuery.Variants(
                                listOf(GetProductsQuery.Edge2(
                                    GetProductsQuery.Node2(
                                        price = "19.99",
                                        sku = "SKU123",
                                    )
                                ))
                            )
                        )
                    )
                )
            )
        )

        val mockProductsResponse = ApolloResponse.Builder(
            operation = GetProductsQuery(),
            data = mockProducts,
            requestUuid = UUID.randomUUID()
        ).build()


        // Mock the response in remoteData
        coEvery { remoteData.getProducts() } returns flowOf(mockProductsResponse)

        // Act: Call the repo method and collect the result
        repo.getProducts().collect { response ->
            assertEquals(mockProducts, response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getProducts() }
    }


    @Test
    fun test_getProducts_returns_error_another() = runTest {
        // Arrange: Create a mock error
        val mockError = Error(
            message = "Failed to fetch products",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        // Mock an ApolloResponse with an error
        val mockErrorResponse = ApolloResponse.Builder(
            operation = GetProductsQuery(),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.getProducts() } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.getProducts().collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to fetch products", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
//        coVerify { remoteData.getProducts() }
    }

    // -------------------------------------- Test HomeProducts --------------------------------------

    @Test
    fun test_HomeProducts_returns_success() = runTest {
        // Arrange: Mock data for home products query
        val mockHomeProducts =  HomeProductsQuery.Data(
            HomeProductsQuery.Products(
                listOf(
                    HomeProductsQuery.Edge(
                        HomeProductsQuery.Node(
                            id = "123",
                            title = "Sneakers",
                            handle = "details about the product",
                            images = HomeProductsQuery.Images(
                                listOf(HomeProductsQuery.Edge1(
                                    HomeProductsQuery.Node1(
                                        src = "https://example.com/sample.jpg"
                                    )
                                ))
                            ),
                            variants = HomeProductsQuery.Variants(
                                listOf(HomeProductsQuery.Edge2(
                                    HomeProductsQuery.Node2(
                                        price = "19.99",
                                    )
                                ))
                            )
                        )
                    )
                )
            )
        )

        val mockHomeProductsResponse = ApolloResponse.Builder(
            operation = HomeProductsQuery(),
            data = mockHomeProducts,
            requestUuid = UUID.randomUUID()
        ).build()


        // Mock the response in remoteData
        coEvery { remoteData.getHomeProducts() } returns flowOf(mockHomeProductsResponse)

        // Act: Call the repo method and collect the result
        repo.getHomeProducts().collect { response ->
            assertEquals(mockHomeProducts, response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getHomeProducts() }
    }


    @Test
    fun test_HomeProducts_returns_error_another() = runTest {
        // Arrange: Create a mock error
        val mockError = Error(
            message = "Failed to fetch home products",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        // Mock an ApolloResponse with an error
        val mockErrorResponse = ApolloResponse.Builder(
            operation = HomeProductsQuery(),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.getHomeProducts() } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.getHomeProducts().collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to fetch home products", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
//        coVerify { remoteData.getHomeProducts() }
    }

    // -------------------------------------- Test FilteredProducts --------------------------------------

    @Test
    fun test_FilteredProducts_returns_success() = runTest {
        // Arrange: Mock data for home products query
        val mockFilteredProducts =  FilteredProductsQuery.Data(
            FilteredProductsQuery.Products(
                listOf(
                    FilteredProductsQuery.Edge(
                        FilteredProductsQuery.Node(
                            id = "123",
                            title = "Sneakers",
                            tags = listOf("man","summer","shoes"),
                            handle = "details about the product",
                            images = FilteredProductsQuery.Images(
                                listOf(FilteredProductsQuery.Edge1(
                                    FilteredProductsQuery.Node1(
                                        src = "https://example.com/sample.jpg"
                                    )
                                ))
                            ),
                            variants = FilteredProductsQuery.Variants(
                                listOf(FilteredProductsQuery.Edge2(
                                    FilteredProductsQuery.Node2(
                                        price = "19.99",
                                    )
                                ))
                            )
                        )
                    )
                )
            )
        )

        val mockFilteredProductsResponse = ApolloResponse.Builder(
            operation = FilteredProductsQuery(),
            data = mockFilteredProducts,
            requestUuid = UUID.randomUUID()
        ).build()


        // Mock the response in remoteData
        coEvery { remoteData.getFilterdProducts("vendor") } returns flowOf(mockFilteredProductsResponse)

        // Act: Call the repo method and collect the result
        repo.getFilterdProducts("vendor").collect { response ->
            assertEquals(mockFilteredProducts, response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getFilterdProducts("vendor") }
    }

    @Test
    fun test_FilteredProducts_withoutVendor_returns_success() = runTest {
        // Arrange: Mock data for home products query
        val mockFilteredProducts =  FilteredProductsQuery.Data(
            FilteredProductsQuery.Products(
                listOf(
                    FilteredProductsQuery.Edge(
                        FilteredProductsQuery.Node(
                            id = "123",
                            title = "Sneakers",
                            tags = listOf("man","summer","shoes"),
                            handle = "details about the product",
                            images = FilteredProductsQuery.Images(
                                listOf(FilteredProductsQuery.Edge1(
                                    FilteredProductsQuery.Node1(
                                        src = "https://example.com/sample.jpg"
                                    )
                                ))
                            ),
                            variants = FilteredProductsQuery.Variants(
                                listOf(FilteredProductsQuery.Edge2(
                                    FilteredProductsQuery.Node2(
                                        price = "19.99",
                                    )
                                ))
                            )
                        )
                    )
                )
            )
        )

        val mockFilteredProductsResponse = ApolloResponse.Builder(
            operation = FilteredProductsQuery(),
            data = mockFilteredProducts,
            requestUuid = UUID.randomUUID()
        ).build()


        // Mock the response in remoteData
        coEvery { remoteData.getFilterdProducts(null) } returns flowOf(mockFilteredProductsResponse)

        // Act: Call the repo method and collect the result
        repo.getFilterdProducts(null).collect { response ->
            assertEquals(mockFilteredProducts, response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getFilterdProducts(null) }
    }

    @Test
    fun test_FilteredProducts_returns_error() = runTest {
        // Arrange: Create a mock error
        val mockError = Error(
            message = "Failed to fetch filtered products",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        // Mock an ApolloResponse with an error
        val mockErrorResponse = ApolloResponse.Builder(
            operation = FilteredProductsQuery(),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.getFilterdProducts("vendor") } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.getFilterdProducts("vendor").collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to fetch filtered products", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
//        coVerify { remoteData.getFilterdProducts("vendor") }
    }

    // -------------------------------------- Test GetOrders --------------------------------------

    @Test
    fun test_GetOrder_returns_success() = runTest {
        // Arrange: Mock data for home products query
        val mockGetOrder =  GetOrdersByCustomerQuery.Data(
            GetOrdersByCustomerQuery.Orders(
             listOf(
                 GetOrdersByCustomerQuery.Edge(
                     GetOrdersByCustomerQuery.Node(
                         id = "123",
                         name = "order 1",
                         createdAt = "2023-10-10T00:00:00Z",
                         billingAddress = GetOrdersByCustomerQuery.BillingAddress(
                             city = "New York",
                             address1 = "123 Main St",
                             address2 = "Apt 4",
                         ),
                         totalPriceSet = GetOrdersByCustomerQuery.TotalPriceSet(
                             shopMoney = GetOrdersByCustomerQuery.ShopMoney(
                                 amount = "19.99",
                                 currencyCode = "USD",
                             )
                         ),
                     )
                 )
             )
            )
        )

        val mockGetOrderResponse = ApolloResponse.Builder(
            operation = GetOrdersByCustomerQuery("email"),
            data = mockGetOrder,
            requestUuid = UUID.randomUUID()
        ).build()


        // Mock the response in remoteData
        coEvery { remoteData.getOrders("email") } returns flowOf(mockGetOrderResponse)

        // Act: Call the repo method and collect the result
        repo.getOrders("email").collect { response ->
            assertEquals(mockGetOrder, response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getOrders("email") }
    }

    @Test
    fun test_GetOrder_returns_error() = runTest {
        // Arrange: Create a mock error
        val mockError = Error(
            message = "Failed to fetch orders",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        // Mock an ApolloResponse with an error
        val mockErrorResponse = ApolloResponse.Builder(
            operation = GetOrdersByCustomerQuery("email"),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.getOrders("email") } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.getOrders("email").collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to fetch orders", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
//        coVerify { remoteData.getOrders("email") }
    }

    // -------------------------------------- Test GetOrderByID --------------------------------------

    @Test
    fun test_GetOrderByID_returns_success() = runTest {
        // Arrange: Mock data for home products query
        val mockGetOrder =  OrderByIdQuery.Data(
            OrderByIdQuery.Order(
                billingAddress = OrderByIdQuery.BillingAddress(
                    city = "New York",
                    address1 = "123 Main St",
                    address2 = "Apt 4",
                ),
                lineItems = OrderByIdQuery.LineItems(
                  listOf(
                      OrderByIdQuery.Edge(
                          OrderByIdQuery.Node(
                             name = "order 1",
                              quantity = 1,
                              image = OrderByIdQuery.Image(
                               src = "https://example.com/image.png"
                              ),
                              originalUnitPriceSet = OrderByIdQuery.OriginalUnitPriceSet(
                                  shopMoney = OrderByIdQuery.ShopMoney(
                                      amount = "19.99",
                                      currencyCode = "USD",
                                  )
                              )
                          )
                      )
                  )
                ),
                totalPriceSet = OrderByIdQuery.TotalPriceSet(
                    shopMoney = OrderByIdQuery.ShopMoney1(
                        amount = "19.99",
                        currencyCode = "USD",
                    )
                )
            )
        )

        val mockGetOrderResponse = ApolloResponse.Builder(
            operation = OrderByIdQuery("id"),
            data = mockGetOrder,
            requestUuid = UUID.randomUUID()
        ).build()


        // Mock the response in remoteData
        coEvery { remoteData.getOrderById("id") } returns flowOf(mockGetOrderResponse)

        // Act: Call the repo method and collect the result
        repo.getOrderById("id").collect { response ->
            assertEquals(mockGetOrder, response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getOrderById("id") }
    }

    @Test
    fun test_GetOrderByID_returns_error() = runTest {
        // Arrange: Create a mock error
        val mockError = Error(
            message = "Failed to fetch order",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        // Mock an ApolloResponse with an error
        val mockErrorResponse = ApolloResponse.Builder(
            operation = OrderByIdQuery("id"),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.getOrderById("id") } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.getOrderById("id").collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to fetch order", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getOrderById("id") }
    }

    // -------------------------------------- Test createDraftOrder --------------------------------------

    @Test
    fun test_createDraftOrder_returns_success() = runTest {
        // Arrange: Mock data for draft order creation
        val mockDraftOrderData = DraftOrderCreateMutation.Data(
            DraftOrderCreateMutation.DraftOrderCreate(
                draftOrder = DraftOrderCreateMutation.DraftOrder(
                    id = "new-draft-order-id",
                ),
                userErrors = listOf(
                    DraftOrderCreateMutation.UserError(
                        message = "Failed to create draft order",
                        field = listOf("error1","error 2")
                    ),
                ),
            )
        )

        val mockDraftOrderResponse = ApolloResponse.Builder(
            operation = DraftOrderCreateMutation(DraftOrderInput()),
            data = mockDraftOrderData,
            requestUuid = UUID.randomUUID()
        ).build()

        // Mock the response in remoteData
        coEvery { remoteData.createDraftOrder(any()) } returns flowOf(mockDraftOrderResponse)

        // Act: Call the repo method and collect the result
        repo.createDraftOrder(DraftOrderInput()).collect { response ->
            assertEquals(mockDraftOrderData, response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.createDraftOrder(any()) }
    }

    @Test
    fun test_createDraftOrder_returns_error() = runTest {
        // Arrange: Create a mock error
        val mockError = Error(
            message = "Failed to create draft order",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        // Mock an ApolloResponse with an error
        val mockErrorResponse = ApolloResponse.Builder(
            operation = DraftOrderCreateMutation(DraftOrderInput()),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.createDraftOrder(any()) } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.createDraftOrder(DraftOrderInput()).collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to create draft order", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.createDraftOrder(any()) }
    }


    // -------------------------------------- Test getDraftOrderByCustomerId --------------------------------------
    @Test
    fun test_getDraftOrderByCustomerId_returns_success() = runTest {
        val mockData = GetDraftOrdersByCustomerQuery.Data(
            draftOrders = GetDraftOrdersByCustomerQuery.DraftOrders(
                nodes = listOf(
                    GetDraftOrdersByCustomerQuery.Node(
                        id = "draftOrder123",
                        tags = listOf("tag1", "tag2"),
                        lineItems = GetDraftOrdersByCustomerQuery.LineItems(
                            nodes = listOf(
                                GetDraftOrdersByCustomerQuery.Node1(
                                    name = "Product Name",
                                    quantity = 2,
                                    title = "Product Title",
                                    variant = GetDraftOrdersByCustomerQuery.Variant(
                                        id = "variant123",
                                        inventoryQuantity = 100,
                                        selectedOptions = listOf(
                                            GetDraftOrdersByCustomerQuery.SelectedOption(
                                                name = "Size",
                                                value = "Medium"
                                            ),
                                            GetDraftOrdersByCustomerQuery.SelectedOption(
                                                name = "Color",
                                                value = "Blue"
                                            )
                                        ),
                                        price = "29.99",
                                        product = GetDraftOrdersByCustomerQuery.Product(
                                            media = GetDraftOrdersByCustomerQuery.Media(
                                                nodes = listOf(
                                                    GetDraftOrdersByCustomerQuery.Node2(
                                                       onMediaImage = GetDraftOrdersByCustomerQuery.OnMediaImage(
                                                        image = GetDraftOrdersByCustomerQuery.Image(
                                                            url = "https://example.com/image.jpg"
                                                        )
                                                       ),
                                                        __typename = "Image"
                                                    )
                                                )
                                            )
                                        )
                                    )
                                ),
                                // Add more line items as needed
                            )
                        )
                    ),
                    // Add more draft orders as needed
                )
            )
        )

        val mockResponse = ApolloResponse.Builder(
            operation = GetDraftOrdersByCustomerQuery("customerId"),
            data = mockData,
            requestUuid = UUID.randomUUID()
        ).build()

        coEvery { remoteData.getDraftOrderByCustomerId("customerId") } returns flowOf(mockResponse)

        repo.getDraftOrderByCustomerId("customerId").collect { response ->
            assertEquals(mockData, response.data)
        }

        coVerify { remoteData.getDraftOrderByCustomerId("customerId") }
    }


    @Test
    fun test_getDraftOrderByCustomerId_returns_error() = runTest {
        // Arrange: Create a mock error
        val mockError = Error(
            message = "Failed to fetch draft orders",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        // Mock an ApolloResponse with an error
        val mockErrorResponse = ApolloResponse.Builder(
            operation = GetDraftOrdersByCustomerQuery("customerId"),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.getDraftOrderByCustomerId("customerId") } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.getDraftOrderByCustomerId("customerId").collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to fetch draft orders", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.getDraftOrderByCustomerId("customerId") }
    }

    // -------------------------------------- Test deleteDraftOrder --------------------------------------

    @Test
    fun test_deleteDraftOrder_returns_success() = runTest {
        val mockData = DeleteDraftOrderMutation.Data(
            DeleteDraftOrderMutation.DraftOrderDelete(
                deletedId = "draftOrder123",
            )
        )

        val mockResponse = ApolloResponse.Builder(
            operation = DeleteDraftOrderMutation(DraftOrderDeleteInput("draftOrder123")),
            data = mockData,
            requestUuid = UUID.randomUUID()
        ).build()

        coEvery { remoteData.deleteDraftOrder(any()) } returns flowOf(mockResponse)

        repo.deleteDraftOrder(DraftOrderDeleteInput("draftOrder123")).collect { response ->
            assertEquals(mockData, response.data)
        }

        coVerify { remoteData.deleteDraftOrder(any()) }
    }


    @Test
    fun test_deleteDraftOrder_returns_error() = runTest {
        val mockError = Error(
            message = "Failed to delete draft order",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        val mockErrorResponse = ApolloResponse.Builder(
            operation = DeleteDraftOrderMutation(DraftOrderDeleteInput("draftOrder123")),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        // Mock the response in remoteData to return the error
        coEvery { remoteData.deleteDraftOrder(any()) } returns flowOf(mockErrorResponse)

        // Act: Call the repo method and collect the result
        repo.deleteDraftOrder(DraftOrderDeleteInput("draftOrder123")).collect { response ->
            // Assert that the response contains the error and no data
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to delete draft order", response.errors?.first()?.message)
            assertNull(response.data)
        }

        // Verify that the method was called
        coVerify { remoteData.deleteDraftOrder(any()) }
    }

    // -------------------------------------- Test updateDraftOrder --------------------------------------

    @Test
    fun test_updateDraftOrder_returns_success() = runTest {

        val mockData = UpdateDraftOrderMetafieldsMutation.Data(
            UpdateDraftOrderMetafieldsMutation.DraftOrderUpdate(
              draftOrder = UpdateDraftOrderMetafieldsMutation.DraftOrder("draftOrderId123",),
                userErrors = listOf(
                    UpdateDraftOrderMetafieldsMutation.UserError(
                        message = "Failed to update draft order",
                        field = listOf("metafields")

                    )
                )
            )
        )

        val mockResponse = ApolloResponse.Builder(
            operation = UpdateDraftOrderMetafieldsMutation(DraftOrderInput(/* Provide necessary inputs */), "draftOrderId123"),
            data = mockData,
            requestUuid = UUID.randomUUID()
        ).build()

        coEvery { remoteData.updateDraftOrder(any(), any()) } returns flowOf(mockResponse)

        repo.updateDraftOrder(DraftOrderInput(/* Provide necessary inputs */), "draftOrderId123").collect { response ->
            assertEquals(mockData, response.data)
        }

        coVerify { remoteData.updateDraftOrder(any(), any()) }
    }


    @Test
    fun test_updateDraftOrder_returns_error() = runTest {

        val mockError = Error(
            message = "Failed to update draft order",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        val mockErrorResponse = ApolloResponse.Builder(
            operation = UpdateDraftOrderMetafieldsMutation(DraftOrderInput(/* Provide necessary inputs */), "draftOrderId123"),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        coEvery { remoteData.updateDraftOrder(any(), any()) } returns flowOf(mockErrorResponse)

        repo.updateDraftOrder(DraftOrderInput(/* Provide necessary inputs */), "draftOrderId123").collect { response ->
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to update draft order", response.errors?.first()?.message)
            assertNull(response.data)
        }

        coVerify { remoteData.updateDraftOrder(any(), any()) }
    }

    // -------------------------------------- Test createOrderFromDraft --------------------------------------

    @Test
    fun test_createOrderFromDraft_returns_success() = runTest {
        // Mock response data for the CreateOrderFromDraftOrder mutation
        val mockData = CreateOrderFromDraftOrderMutation.Data(
            draftOrderComplete = CreateOrderFromDraftOrderMutation.DraftOrderComplete(
                draftOrder = CreateOrderFromDraftOrderMutation.DraftOrder(
                    id = "draftOrder123",
                    order = CreateOrderFromDraftOrderMutation.Order(
                        id = "order123"
                    )
                )
            )
        )


        val mockResponse = ApolloResponse.Builder(
            operation = CreateOrderFromDraftOrderMutation("draftOrderId123"),
            data = mockData,
            requestUuid = UUID.randomUUID()
        ).build()

        coEvery { remoteData.createOrderFromDraft("draftOrderId123") } returns flowOf(mockResponse)

        repo.createOrderFromDraft("draftOrderId123").collect { response ->
            assertEquals(mockData, response.data)
        }

        coVerify { remoteData.createOrderFromDraft("draftOrderId123") }
    }

    @Test
    fun test_createOrderFromDraft_returns_error() = runTest {
        val mockError = Error(
            message = "Failed to create order from draft",
            locations = null,
            path = null,
            extensions = null,
            nonStandardFields = emptyMap()
        )

        val mockErrorResponse = ApolloResponse.Builder(
            operation = CreateOrderFromDraftOrderMutation("draftOrderId123"),
            data = null,
            requestUuid = UUID.randomUUID()
        )
            .errors(listOf(mockError))
            .build()

        coEvery { remoteData.createOrderFromDraft("draftOrderId123") } returns flowOf(mockErrorResponse)

        repo.createOrderFromDraft("draftOrderId123").collect { response ->
            assertNotNull(response.errors)
            assertTrue(response.hasErrors())
            assertEquals("Failed to create order from draft", response.errors?.first()?.message)
            assertNull(response.data)
        }

        coVerify { remoteData.createOrderFromDraft("draftOrderId123") }
    }

}