package com.example.couturecorner.data.model


// AccessTokenResponse.kt
data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

// OrderRequest.kt
data class OrderRequest(
    val intent: String = "CAPTURE",
    val purchase_units: List<PurchaseUnit>,
    val payment_source: PaymentSource
)

data class PurchaseUnit(
    val reference_id: String,
    val amount: Amount
)
data class CaptureRequest(
    val amount: Amount
)


data class Amount(
    val currency_code: String,
    val value: String
)

data class PaymentSource(
    val paypal: PayPalExperience
)

data class PayPalExperience(
    val experience_context: ExperienceContext
)

data class ExperienceContext(
    val payment_method_preference: String,
    val brand_name: String,
    val locale: String,
    val landing_page: String,
    val shipping_preference: String,
    val user_action: String,
    val return_url: String,
    val cancel_url: String
)

// OrderResponse.kt
data class OrderResponse(
    val id: String,
    val status: String,
    val links: List<Link> // Ensure this property matches the API response

)

data class Link(
    val href: String
)