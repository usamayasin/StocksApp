package com.example.stocksapp.domain.usecase

import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.repository.StocksRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class StocksUseCasesTest {

    @Test
    fun `ConnectStocksFeedUseCase delegates to repository connect`() = runBlocking {
        val repository = mockk<StocksRepository>()
        coEvery { repository.connect() } returns Unit
        val useCase = ConnectStocksFeedUseCase(repository)

        useCase()

        coVerify(exactly = 1) { repository.connect() }
    }

    @Test
    fun `DisconnectStocksFeedUseCase delegates to repository disconnect`() = runBlocking {
        val repository = mockk<StocksRepository>()
        coEvery { repository.disconnect() } returns Unit
        val useCase = DisconnectStocksFeedUseCase(repository)

        useCase()

        coVerify(exactly = 1) { repository.disconnect() }
    }

    @Test
    fun `ObserveStockPricesUseCase returns repository flow`() = runBlocking {
        val stock = Stock(
            symbol = "AAPL",
            companyName = "Apple Inc.",
            logoUrl = null,
            price = 175.50,
            change = 1.25,
        )
        val pricesFlow = flowOf(stock)
        val repository = mockk<StocksRepository>()
        every { repository.observePriceUpdates() } returns pricesFlow
        val useCase = ObserveStockPricesUseCase(repository)

        val resultFlow = useCase()

        assertSame(pricesFlow, resultFlow)
        assertEquals(stock, resultFlow.first())
    }

    @Test
    fun `ObserveConnectionStatusUseCase returns repository flow`() = runBlocking {
        val statusFlow = flowOf(ConnectionStatus.Connected)
        val repository = mockk<StocksRepository>()
        every { repository.observeConnectionStatus() } returns statusFlow
        val useCase = ObserveConnectionStatusUseCase(repository)

        val resultFlow = useCase()

        assertSame(statusFlow, resultFlow)
        assertTrue(resultFlow.first() is ConnectionStatus.Connected)
    }
}
