package com.example.stocksapp.domain.usecase

import com.example.stocksapp.domain.repository.StocksRepository
import javax.inject.Inject

class DisconnectStocksFeedUseCase @Inject constructor(
    private val stocksRepository: StocksRepository,
) {
    suspend operator fun invoke() {
        stocksRepository.disconnect()
    }
}
