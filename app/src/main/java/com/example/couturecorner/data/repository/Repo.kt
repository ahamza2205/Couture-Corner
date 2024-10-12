package com.example.couturecorner.data.repository
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.model.ConvertResponse
import com.example.couturecorner.data.remote.CurrencyApiService
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.network.MyApolloClient
import com.example.couturecorner.network.MyApolloClient.apolloClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.graphql.AddFavoriteProductMutation
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.CustomerCreateMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetCustomerByEmailQuery
import com.graphql.GetCustomerByIdQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.GetFavoriteProductsQuery
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.OrderByIdQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.ProductQuery
import com.graphql.UpdateDraftOrderMetafieldsMutation
import com.graphql.type.CustomerInput
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class Repo @Inject constructor(
    private val remoteData: IremoteData,
    private val sharedPreference: SharedPreference,
    private val apiService: CurrencyApiService,
    private val apolloClient: ApolloClient
) : Irepo {

    override fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>> {
        return remoteData.getProducts()
    }

    override fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>> {
        return remoteData.getHomeProducts()
    }

    override fun getFilterdProducts(vendor: String?): Flow<ApolloResponse<FilteredProductsQuery.Data>> {
        return remoteData.getFilterdProducts(vendor)
    }

// ---------------------------- Draft Order ------------------------------------

    override fun createDraftOrder(input: DraftOrderInput): Flow<ApolloResponse<DraftOrderCreateMutation.Data>> {

        return remoteData.createDraftOrder(input)
    }

    override fun getDraftOrderByCustomerId(id: String): Flow<ApolloResponse<GetDraftOrdersByCustomerQuery.Data>> {

        return remoteData.getDraftOrderByCustomerId(id)
    }

    override fun deleteDraftOrder(input: DraftOrderDeleteInput): Flow<ApolloResponse<DeleteDraftOrderMutation.Data>> {

        return remoteData.deleteDraftOrder(input)
    }


    override fun updateDraftOrder(
        input: DraftOrderInput,
        id: String
    ): Flow<ApolloResponse<UpdateDraftOrderMetafieldsMutation.Data>> {

        return remoteData.updateDraftOrder(input, id)
    }


    override fun createOrderFromDraft(id: String): Flow<ApolloResponse<CreateOrderFromDraftOrderMutation.Data>>{

        return remoteData.createOrderFromDraft(id)
    }

    override fun getOrders(emai: String): Flow<ApolloResponse<GetOrdersByCustomerQuery.Data>> {

        return remoteData.getOrders(emai)
    }

    override fun getOrderById(id: String): Flow<ApolloResponse<OrderByIdQuery.Data>> {

        return remoteData.getOrderById(id)
    }


    // ---------------------------- shared preference ------------------------------------
    // ---------------------------- shared preference ------------------------------------
    override fun saveUserLoggedIn(isLoggedIn: Boolean) {
        sharedPreference.saveUserLoggedIn(isLoggedIn)
    }

    override fun isUserLoggedIn(): Boolean {
        return sharedPreference.isUserLoggedIn()
    }

    override fun logoutUser() {
        sharedPreference.logoutUser()
    }


    override fun saveDraftOrderId(userId: String, ID: String) {
        sharedPreference.saveDraftOrderId(userId, ID)
    }

    override fun getDraftOrderId(userId: String): String? {
        return sharedPreference.getDraftOrderId(userId)}

    override fun deleteDraftOrderId(userId: String) {
        sharedPreference.deleteDraftOrderId(userId)
    }


    // --------------------------- shopify registration -------------------------------
    suspend fun registerUser(
        email: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
        idToken: String? = null,
    ): String? {
        if (email.isNullOrEmpty() || firstName.isNullOrEmpty() || lastName.isNullOrEmpty()) {
            throw IllegalArgumentException("All fields except password and phoneNumber must be provided.")
        }

        val auth = FirebaseAuth.getInstance()
        try {
            // Create Firebase user
            val firebaseUserId = if (password != null) {
                auth.createUserWithEmailAndPassword(email, password).await()
            } else if (idToken != null) {
                // Sign in using Google
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
            } else {
                throw IllegalArgumentException("Must provide either a password or an idToken for registration.")
            }

            if (firebaseUserId.user != null) {
                // Proceed with Shopify registration
                val client = MyApolloClient.apolloClient
                val mutation = CustomerCreateMutation(
                    input = CustomerInput(
                        email = Optional.Present(email),
                        firstName = Optional.Present(firstName),
                        lastName = Optional.Present(lastName),
                        phone = if (!phoneNumber.isNullOrEmpty()) Optional.Present(phoneNumber) else Optional.Absent,
                        tags = Optional.Absent,
                        metafields = Optional.Absent,
                        addresses = Optional.Absent
                    )
                )
                val response = client.mutation(mutation).execute()

                if (response.hasErrors()) {
                    Log.e("ShopifyRegistration", "Shopify user creation failed: ${response.errors}")
                    throw Exception("Error creating Shopify user: ${response.errors}")
                } else {
                    val shopifyUserId = response.data?.customerCreate?.customer?.id
                    Log.d("UserRegistration", "User successfully created on Shopify: $firstName $lastName, Shopify User ID: $shopifyUserId")

                    // Fetch the customer ID by email
                    val customerId = getCustomerByEmail(email)
                    if (customerId != null) {
                        // Save the customer ID in shared preferences
                        sharedPreference.saveShopifyUserId(email, customerId)
                        Log.d("UserRegistration", "Shopify User ID saved in SharedPreferences: $customerId")
                    } else {
                        Log.e("UserRegistration", "Failed to fetch Shopify user ID")
                    }

                    return shopifyUserId
                }
            } else {
                Log.e("UserRegistration", "Failed to create Firebase user")
                throw Exception("Failed to create Firebase user")
            }
        } catch (e: Exception) {
            Log.e("UserRegistration", "Error in registration process: ${e.message}")
            throw e
        }
    }
    // --------------------------- get customer data  --------------------------------
    // --------------------------- get customer by email --------------------------------
    suspend fun getCustomerByEmail(email: String): String? {
        val client = MyApolloClient.apolloClient
        val response = client.query(GetCustomerByEmailQuery(email = email)).execute()

        if (response.hasErrors()) {
            Log.e("GetCustomerByEmail", "Error fetching customer by email: ${response.errors}")
            throw Exception("Error fetching customer by email: ${response.errors}")
        }

        // Extract customer ID from the response
        val customerId = response.data?.customers?.edges?.firstOrNull()?.node?.id
        return customerId
    }

    // --------------------------- get customer by id --------------------------------
    suspend fun getCustomerById(customerId: String): GetCustomerByIdQuery.Customer? {
        val client = MyApolloClient.apolloClient
        val response = client.query(GetCustomerByIdQuery(id = customerId)).execute()

        if (response.hasErrors()) {
            Log.e("CustomerData", "Error fetching customer: ${response.errors}")
            throw Exception("Error fetching customer: ${response.errors}")
        }

        return response.data?.customer?.let { customer ->
            GetCustomerByIdQuery.Customer(
                id = customer.id,
                displayName = customer.displayName ?: "", // Handle nullable fields
                email = customer.email,
                firstName = customer.firstName,
                lastName = customer.lastName,
                phone = customer.phone,
                createdAt = customer.createdAt,
                updatedAt = customer.updatedAt,
                defaultAddress = customer.defaultAddress?.let { address ->
                    GetCustomerByIdQuery.DefaultAddress( // Correctly reference the nested DefaultAddress class
                        address1 = address.address1 ?: "", // Handle nullable fields
                        address2 = address.address2 ?: "",
                        city = address.city ?: "",
                        phone = address.phone ?: ""
                    )
                },
                addresses = customer?.addresses
            )
        }
    }

    // ----------------------------------- product details --------------------------------
    override  suspend fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Data?>>  {
       return remoteData.getProductDetails(productId)
    }


    // ------------------------ get & save shopify user id --------------------------------
    override fun getShopifyUserId(email: String): String? {
        return sharedPreference.getShopifyUserId(email)
    }

    override fun saveShopifyUserId(email: String, userId: String) {
        sharedPreference.saveShopifyUserId(email, userId)
    }

    override fun saveDraftOrderTag(userId: String, tag: String) {
        sharedPreference.saveDraftOrderTag(userId, tag)
    }

    override fun getDraftOrderTag(userId: String): String? {
        return sharedPreference.getDraftOrderTag(userId)
    }

    // --------------------------- Add product to favorite --------------------------------
    override suspend fun addProductToFavorites(customerId: String, productId: String) {
        return remoteData.addProductToFavorites(customerId, productId)
    }

   override suspend fun getCurrentFavorites(customerId: String): List<String>? {
        return remoteData.getCurrentFavorites(customerId)
    }
    override suspend fun removeProductFromFavorites(customerId: String, productId: String) {
        return remoteData.removeProductFromFavorites(customerId, productId)
    }


    // ----------------------------------- cupones --------------------------------
    override fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>> {
        return remoteData.getCupones()
    }
    override fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>> {
        return remoteData.updateCustomer(input)
    }
   // ----------------------------------- currency --------------------------------


    suspend fun convertCurrency(from: String, to: String, amount: Double, apiKey: String): ConvertResponse? {
        Log.d("CurrencyConversion", "Request to convert currency: From = $from, To = $to, Amount = $amount")

        val response = apiService.convertCurrency(from, to, amount, apiKey)

        Log.d("CurrencyConversion", "API Response: ${response.code()} - ${response.message()}")

        return if (response.isSuccessful) {
            Log.d("CurrencyConversion", "Successful conversion: ${response.body()}")
            response.body()
        } else {
            Log.e("CurrencyConversion", "Failed to convert currency. Error: ${response.errorBody()?.string()}")
            null
        }
    }


    fun saveSelectedCurrency(currency: String) {
        sharedPreference.saveSelectedCurrency(currency)
    }

    fun getSelectedCurrency(): String? {
        return sharedPreference.getSelectedCurrency()
    }
    }





