package com.example.stocksapp.domain.model

data class Stock(
    val symbol: String,
    val companyName: String,
    val price: Double,
    val change: Double
) {
    val direction: PriceDirection
        get() = when {
            change > 0.01 -> PriceDirection.UP
            change < -0.01 -> PriceDirection.DOWN
            else -> PriceDirection.UNCHANGED
        }
}