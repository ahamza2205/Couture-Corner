package com.example.couturecorner.repo

// Mock data class for ProductQuery response
data class MockProductQueryResponse(
    val product: MockProduct?
)

data class MockProduct(
    val description: String?,
    val id: String,
    val productType: String?,
    val title: String,
    val totalInventory: Int?,
    val vendor: String?,
    val images: MockImages?,
    val variants: MockVariants?
)

data class MockImages(
    val edges: List<MockImageEdge?>?
)

data class MockImageEdge(
    val node: MockImageNode?
)

data class MockImageNode(
    val src: String
)

data class MockVariants(
    val edges: List<MockVariantEdge?>?
)

data class MockVariantEdge(
    val node: MockVariantNode?
)

data class MockVariantNode(
    val id: String,
    val displayName: String?,
    val price: String,
    val sku: String?,
    val selectedOptions: List<MockSelectedOption?>?
)

data class MockSelectedOption(
    val name: String?,
    val value: String?
)
