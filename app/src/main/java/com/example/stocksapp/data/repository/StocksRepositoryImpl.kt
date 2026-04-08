package com.example.stocksapp.data.repository

import com.example.stocksapp.data.mapper.StockDtoToDomainMapper
import com.example.stocksapp.data.mapper.toDomain
import com.example.stocksapp.data.network.WebSocketDataSource
import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.repository.StocksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StocksRepositoryImpl @Inject constructor(
    private val webSocketDataSource: WebSocketDataSource,
    private val dtoToDomainMapper: StockDtoToDomainMapper
) : StocksRepository {

    override suspend fun connect() = webSocketDataSource.connect()

    override suspend fun disconnect() = webSocketDataSource.disconnect()

    override fun observePriceUpdates(): Flow<Stock> =
        webSocketDataSource.observePriceUpdates().map { dto ->
            dtoToDomainMapper.toDomain(dto)
        }

    override fun observeConnectionStatus(): Flow<ConnectionStatus> =
        webSocketDataSource.observeConnectionStatus().map { it.toDomain() }
}
