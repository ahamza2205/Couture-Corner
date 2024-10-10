package com.example.couturecorner.data.model

data class ConvertResponse(
    val base: String,
    val amount: Double,
    val result: Map<String, Double>
)
