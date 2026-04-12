package com.example.stocksapp.domain.repository

import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import kotlinx.coroutines.flow.Flow

interface StocksRepository {

    /**
     * Opens the stock feed transport (e.g. WebSocket). Errors surface via [observeConnectionStatus].
     */
    suspend fun connect()

    /**
     * Stops the feed and closes transport; no further price updates until [connect].
     */
    suspend fun disconnect()

    /**
     * Cold [Flow] of price ticks; collectors receive updates while connected.
     */
    fun observePriceUpdates(): Flow<Stock>

    /**
     * Cold [Flow] of connection lifecycle: [ConnectionStatus.Connecting] → [ConnectionStatus.Connected]
     * or [ConnectionStatus.Error], and [ConnectionStatus.Disconnected] after [disconnect].
     */
    fun observeConnectionStatus(): Flow<ConnectionStatus>
}
