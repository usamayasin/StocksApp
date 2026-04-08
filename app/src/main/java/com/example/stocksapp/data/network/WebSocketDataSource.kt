package com.example.stocksapp.data.network

import com.example.stocksapp.data.model.ConnectionState
import com.example.stocksapp.data.model.StockDto
import kotlinx.coroutines.flow.Flow

interface WebSocketDataSource {

    suspend fun connect()

    suspend fun disconnect()

    suspend fun sendPriceUpdate(priceUpdate: StockDto)

    fun observePriceUpdates(): Flow<StockDto>

    fun observeConnectionStatus(): Flow<ConnectionState>

    fun isConnected(): Boolean
}
