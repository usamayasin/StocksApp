package com.example.stocksapp.ui.stocks

import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.usecase.ConnectStocksFeedUseCase
import com.example.stocksapp.domain.usecase.DisconnectStocksFeedUseCase
import com.example.stocksapp.domain.usecase.ObserveConnectionStatusUseCase
import com.example.stocksapp.domain.usecase.ObserveStockPricesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StocksListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stocks list is merged by symbol and sorted by descending price`() = runTest {
        val pricesFlow = MutableSharedFlow<Stock>(replay = 1, extraBufferCapacity = 4)
        val connectionFlow = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
        val connectUseCase = mockk<ConnectStocksFeedUseCase>()
        val disconnectUseCase = mockk<DisconnectStocksFeedUseCase>()
        val observePricesUseCase = mockk<ObserveStockPricesUseCase>()
        val observeConnectionUseCase = mockk<ObserveConnectionStatusUseCase>()
        coEvery { connectUseCase() } returns Unit
        coEvery { disconnectUseCase() } returns Unit
        every { observePricesUseCase() } returns pricesFlow
        every { observeConnectionUseCase() } returns connectionFlow
        val viewModel = StocksListViewModel(
            connectStocksFeed = connectUseCase,
            disconnectStocksFeed = disconnectUseCase,
            observeStockPrices = observePricesUseCase,
            observeConnectionStatus = observeConnectionUseCase,
        )
        val collector = launch { viewModel.stocks.collect {} }
        advanceUntilIdle()

        pricesFlow.tryEmit(
            Stock(
                symbol = "AAPL",
                companyName = "Apple Inc.",
                logoUrl = null,
                price = 100.0,
                change = 1.0,
            ),
        )
        pricesFlow.tryEmit(
            Stock(
                symbol = "TSLA",
                companyName = "Tesla",
                logoUrl = null,
                price = 200.0,
                change = 2.0,
            ),
        )
        pricesFlow.tryEmit(
            Stock(
                symbol = "AAPL",
                companyName = "Apple Inc.",
                logoUrl = null,
                price = 300.0,
                change = 3.0,
            ),
        )
        advanceUntilIdle()

        assertEquals(listOf("AAPL", "TSLA"), viewModel.stocks.value.map { it.symbol })
        assertEquals(300.0, viewModel.stocks.value.first().price, 0.0)
        collector.cancel()
    }

    @Test
    fun `connection status emits latest value from use case`() = runTest {
        val pricesFlow = MutableSharedFlow<Stock>(replay = 1, extraBufferCapacity = 1)
        val connectionFlow = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Connecting)
        val connectUseCase = mockk<ConnectStocksFeedUseCase>()
        val disconnectUseCase = mockk<DisconnectStocksFeedUseCase>()
        val observePricesUseCase = mockk<ObserveStockPricesUseCase>()
        val observeConnectionUseCase = mockk<ObserveConnectionStatusUseCase>()
        coEvery { connectUseCase() } returns Unit
        coEvery { disconnectUseCase() } returns Unit
        every { observePricesUseCase() } returns pricesFlow
        every { observeConnectionUseCase() } returns connectionFlow
        val viewModel = StocksListViewModel(
            connectStocksFeed = connectUseCase,
            disconnectStocksFeed = disconnectUseCase,
            observeStockPrices = observePricesUseCase,
            observeConnectionStatus = observeConnectionUseCase,
        )
        advanceUntilIdle()

        connectionFlow.value = ConnectionStatus.Connected
        advanceUntilIdle()

        assertTrue(viewModel.connectionStatus.value is ConnectionStatus.Connected)
    }

    @Test
    fun `feed toggle disconnects when currently connected`() = runTest {
        val pricesFlow = MutableSharedFlow<Stock>(replay = 1, extraBufferCapacity = 1)
        val connectionFlow = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Connected)
        val connectUseCase = mockk<ConnectStocksFeedUseCase>()
        val disconnectUseCase = mockk<DisconnectStocksFeedUseCase>()
        val observePricesUseCase = mockk<ObserveStockPricesUseCase>()
        val observeConnectionUseCase = mockk<ObserveConnectionStatusUseCase>()
        coEvery { connectUseCase() } returns Unit
        coEvery { disconnectUseCase() } returns Unit
        every { observePricesUseCase() } returns pricesFlow
        every { observeConnectionUseCase() } returns connectionFlow
        val viewModel = StocksListViewModel(
            connectStocksFeed = connectUseCase,
            disconnectStocksFeed = disconnectUseCase,
            observeStockPrices = observePricesUseCase,
            observeConnectionStatus = observeConnectionUseCase,
        )
        advanceUntilIdle()

        viewModel.onFeedToggleClicked()
        advanceUntilIdle()

        coVerify(exactly = 1) { disconnectUseCase() }
    }
}
