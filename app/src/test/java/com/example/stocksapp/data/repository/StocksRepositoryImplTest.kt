package com.example.stocksapp.data.repository

import com.example.stocksapp.data.mapper.StockDtoToDomainMapper
import com.example.stocksapp.data.model.ConnectionState
import com.example.stocksapp.data.model.StockDto
import com.example.stocksapp.data.network.WebSocketDataSource
import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StocksRepositoryImplTest {

    @Test
    fun `connect and disconnect delegate to websocket data source`() = runBlocking {
        val dataSource = mockk<WebSocketDataSource>()
        val mapper = mockk<StockDtoToDomainMapper>()
        coEvery { dataSource.connect() } returns Unit
        coEvery { dataSource.disconnect() } returns Unit
        val repository = StocksRepositoryImpl(dataSource, mapper)

        repository.connect()
        repository.disconnect()

        coVerify(exactly = 1) { dataSource.connect() }
        coVerify(exactly = 1) { dataSource.disconnect() }
    }

    @Test
    fun `observePriceUpdates maps StockDto to Stock`() = runBlocking {
        val dto = StockDto(
            symbol = "GOOG",
            companyName = "Google",
            logoUrl = null,
            price = 155.0,
            previousPrice = 150.0,
            timestamp = 99L,
        )
        val mapped = Stock(
            symbol = "GOOG",
            companyName = "Google",
            logoUrl = null,
            price = 155.0,
            change = 5.0,
        )
        val dataSource = mockk<WebSocketDataSource>()
        val mapper = mockk<StockDtoToDomainMapper>()
        every { dataSource.observePriceUpdates() } returns flowOf(dto)
        every { mapper.toDomain(dto) } returns mapped
        val repository = StocksRepositoryImpl(dataSource, mapper)

        val stock = repository.observePriceUpdates().first()

        assertEquals(mapped, stock)
    }

    @Test
    fun `observeConnectionStatus maps transport state to domain state`() = runBlocking {
        val dataSource = mockk<WebSocketDataSource>()
        val mapper = mockk<StockDtoToDomainMapper>()
        every { dataSource.observeConnectionStatus() } returns flowOf(ConnectionState.Error("No internet"))
        val repository = StocksRepositoryImpl(dataSource, mapper)

        val status = repository.observeConnectionStatus().first()

        assertTrue(status is ConnectionStatus.Error)
        assertEquals("No internet", (status as ConnectionStatus.Error).message)
    }
}
