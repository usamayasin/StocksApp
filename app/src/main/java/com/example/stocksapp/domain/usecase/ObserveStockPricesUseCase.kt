package com.example.stocksapp.domain.usecase

import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.repository.StocksRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveStockPricesUseCase @Inject constructor(
    private val stocksRepository: StocksRepository,
) {
    operator fun invoke(): Flow<Stock> = stocksRepository.observePriceUpdates()
}
