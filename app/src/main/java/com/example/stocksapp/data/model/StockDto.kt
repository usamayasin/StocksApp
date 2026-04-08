package com.example.stocksapp.data.model

import org.json.JSONObject

data class StockDto(
    val symbol: String,
    val companyName: String,
    val price: Double,
    val previousPrice: Double,
    val timestamp: Long
)

fun StockDto.toWireJson(): String =
    JSONObject().apply {
        put("symbol", symbol)
        put("companyName", companyName)
        put("price", price)
        put("previousPrice", previousPrice)
        put("timestamp", timestamp)
    }.toString()

fun parseStockDtoFromWire(text: String): StockDto? =
    try {
        val json = JSONObject(text)
        StockDto(
            symbol = json.getString("symbol"),
            companyName = json.optString("companyName", ""),
            price = json.getDouble("price"),
            previousPrice = json.getDouble("previousPrice"),
            timestamp = json.getLong("timestamp")
        )
    } catch (_: Exception) {
        null
    }
