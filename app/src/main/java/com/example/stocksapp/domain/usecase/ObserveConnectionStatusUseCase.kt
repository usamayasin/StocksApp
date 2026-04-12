package com.example.stocksapp.domain.usecase

import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.repository.StocksRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveConnectionStatusUseCase @Inject constructor(
    private val stocksRepository: StocksRepository,
) {
    operator fun invoke(): Flow<ConnectionStatus> = stocksRepository.observeConnectionStatus()
}
