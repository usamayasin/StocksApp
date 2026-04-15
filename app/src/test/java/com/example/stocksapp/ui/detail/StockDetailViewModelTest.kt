package com.example.stocksapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.usecase.ObserveStockPricesUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockDetailViewModelTest {

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
    fun `stock starts as null`() = runTest {
        val pricesFlow = MutableSharedFlow<Stock>(replay = 1, extraBufferCapacity = 1)
        val observeStockPrices = mockk<ObserveStockPricesUseCase>()
        every { observeStockPrices() } returns pricesFlow
        val savedStateHandle = SavedStateHandle(mapOf("symbol" to "AAPL"))

        val viewModel = StockDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeStockPrices = observeStockPrices,
        )
        advanceUntilIdle()

        assertNull(viewModel.stock.value)
    }

    @Test
    fun `stock updates when symbol matches route`() = runTest {
        val pricesFlow = MutableSharedFlow<Stock>(replay = 1, extraBufferCapacity = 1)
        val observeStockPrices = mockk<ObserveStockPricesUseCase>()
        every { observeStockPrices() } returns pricesFlow
        val savedStateHandle = SavedStateHandle(mapOf("symbol" to "AAPL"))
        val matchingStock = Stock(
            symbol = "AAPL",
            companyName = "Apple Inc.",
            logoUrl = null,
            price = 182.0,
            change = 1.5,
        )

        val viewModel = StockDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeStockPrices = observeStockPrices,
        )
        val collector = launch { viewModel.stock.collect {} }
        advanceUntilIdle()

        pricesFlow.tryEmit(matchingStock)
        advanceUntilIdle()

        assertEquals(matchingStock, viewModel.stock.value)
        collector.cancel()
    }

    @Test
    fun `stock ignores symbols that do not match route`() = runTest {
        val pricesFlow = MutableSharedFlow<Stock>(replay = 1, extraBufferCapacity = 1)
        val observeStockPrices = mockk<ObserveStockPricesUseCase>()
        every { observeStockPrices() } returns pricesFlow
        val savedStateHandle = SavedStateHandle(mapOf("symbol" to "AAPL"))
        val otherStock = Stock(
            symbol = "TSLA",
            companyName = "Tesla",
            logoUrl = null,
            price = 210.0,
            change = -2.0,
        )

        val viewModel = StockDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeStockPrices = observeStockPrices,
        )
        val collector = launch { viewModel.stock.collect {} }
        advanceUntilIdle()

        pricesFlow.tryEmit(otherStock)
        advanceUntilIdle()

        assertNull(viewModel.stock.value)
        collector.cancel()
    }
}
