package com.example.stocksapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object StocksListDestination

@Serializable
data class StockDetailDestination(
    val symbol: String,
)
