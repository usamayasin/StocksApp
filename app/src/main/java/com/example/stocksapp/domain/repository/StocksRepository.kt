package com.example.stocksapp.domain.repository

import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import kotlinx.coroutines.flow.Flow

interface StocksRepository {

    suspend fun connect()

    suspend fun disconnect()

    fun observePriceUpdates(): Flow<Stock>

    fun observeConnectionStatus(): Flow<ConnectionStatus>
}
