package com.example.couturecorner.data.repository
import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.local.SharedPreferenceImp
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.model.ConvertResponse
import com.example.couturecorner.data.remote.CurrencyApiService
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.network.MyApolloClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.CustomerCreateMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetCustomerByEmailQuery
import com.graphql.GetCustomerByIdQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.ProductQuery
import com.graphql.UpdateDraftOrderMetafieldsMutation
import com.graphql.type.CustomerInput
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class Repo @Inject constructor(
    private val remoteData: IremoteData,
    private val sharedPreference: SharedPreference,
    private val apiService: CurrencyApiService,
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


    override fun saveAddressState(haveAddress: Boolean){
        sharedPreference.saveAddressState(haveAddress) }


    override fun getAddressState(): Boolean {
        return sharedPreference.getAddressState()
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
    override  suspend fun registerUser(email: String?, password: String?, firstName: String?, lastName: String?, phoneNumber: String?, idToken: String?, ): String? {
       return  remoteData.registerUser( email , password , firstName , lastName , phoneNumber)
    }
    // --------------------------- get customer by email --------------------------------
    override suspend fun getCustomerByEmail(email: String): String? {
        return remoteData.getCustomerByEmail(email)
    }

    // --------------------------- get customer by id --------------------------------
    override suspend fun getCustomerById(customerId: String): GetCustomerByIdQuery.Customer? {
        return remoteData.getCustomerById(customerId)
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

    }





