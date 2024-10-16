package com.example.couturecorner.data.local



data class Review(
    val id: Int,
    val reviewerName: String,
    val profileImageUrl: String,
    val text: String,
    val rating: Float
)

object LocalListsData
{
    val productIds = listOf(
        "gid://shopify/Product/9783852466460", "gid://shopify/Product/9783852531996",
        "gid://shopify/Product/9783853678876", "gid://shopify/Product/9783855284508",
        "gid://shopify/Product/9783856038172", "gid://shopify/Product/9783856169244",
        "gid://shopify/Product/9783856234780", "gid://shopify/Product/9783857643804",
        "gid://shopify/Product/9783858626844", "gid://shopify/Product/9783858725148",
        "gid://shopify/Product/9783858790684", "gid://shopify/Product/9783858888988",
        "gid://shopify/Product/9783858954524", "gid://shopify/Product/9783859151132",
        "gid://shopify/Product/9783859183900", "gid://shopify/Product/9783859740956",
        "gid://shopify/Product/9783860953372", "gid://shopify/Product/9783862362396",
        "gid://shopify/Product/9783862788380", "gid://shopify/Product/9783862821148",
        "gid://shopify/Product/9783863771420", "gid://shopify/Product/9783865442588",
        "gid://shopify/Product/9783866458396", "gid://shopify/Product/9783866523932",
        "gid://shopify/Product/9783866589468", "gid://shopify/Product/9783866786076",
        "gid://shopify/Product/9783868031260", "gid://shopify/Product/9783869505820",
        "gid://shopify/Product/9783870193948", "gid://shopify/Product/9783870325020"
    )

    val ratings = listOf(4.5f, 4.2f, 4.8f, 4.1f, 4.9f, 4.3f, 4.6f, 4.0f, 4.7f, 4.4f,
        3.9f, 4.1f, 4.5f, 3.8f, 4.2f, 4.6f, 3.7f, 4.3f, 4.0f, 4.8f,
        3.6f, 4.4f, 4.1f, 3.5f, 4.7f, 3.4f, 4.5f, 3.3f, 4.2f, 3.2f)

    val productRatingsMap = productIds.zip(ratings).toMap()


    val sampleReviews = mutableListOf(
        Review(
            id = 1,
            reviewerName = "Alice Smith",
            profileImageUrl = "https://randomuser.me/api/portraits/women/1.jpg",
            text = "Great product! Really helped me stay organized and productive.",
            rating = 4.5f
        ),
        Review(
            id = 2,
            reviewerName = "John Doe",
            profileImageUrl = "https://randomuser.me/api/portraits/men/2.jpg",
            text = "The product is decent, but it could use some improvements in design.",
            rating = 3.0f
        ),
        Review(
            id = 3,
            reviewerName = "Emma Brown",
            profileImageUrl = "https://randomuser.me/api/portraits/women/3.jpg",
            text = "Absolutely love it! The features are exactly what I needed.",
            rating = 5.0f
        ),
        Review(
            id = 4,
            reviewerName = "David Wilson",
            profileImageUrl = "https://randomuser.me/api/portraits/men/4.jpg",
            text = "Good value for the price, but there are better options available.",
            rating = 3.5f
        ),
        Review(
            id = 5,
            reviewerName = "Sophia Davis",
            profileImageUrl = "https://randomuser.me/api/portraits/women/5.jpg",
            text = "The product exceeded my expectations, and the customer service was great!",
            rating = 4.8f
        )
    )

}