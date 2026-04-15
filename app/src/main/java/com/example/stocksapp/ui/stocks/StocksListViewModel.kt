package com.example.stocksapp.ui.stocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.usecase.ConnectStocksFeedUseCase
import com.example.stocksapp.domain.usecase.DisconnectStocksFeedUseCase
import com.example.stocksapp.domain.usecase.ObserveConnectionStatusUseCase
import com.example.stocksapp.domain.usecase.ObserveStockPricesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StocksListViewModel @Inject constructor(
    private val connectStocksFeed: ConnectStocksFeedUseCase,
    private val disconnectStocksFeed: DisconnectStocksFeedUseCase,
    private val observeStockPrices: ObserveStockPricesUseCase,
    private val observeConnectionStatus: ObserveConnectionStatusUseCase,
) : ViewModel() {

    private val _stocksBySymbol = MutableStateFlow<Map<String, Stock>>(emptyMap())

    val stocks: StateFlow<List<Stock>> = _stocksBySymbol
        .map { map -> map.values.sortedByDescending { it.price } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _manuallyStopped = MutableStateFlow(false)

    val showConnectionLoader: StateFlow<Boolean> =
        combine(_connectionStatus, _manuallyStopped) { status, stopped ->
            status is ConnectionStatus.Connecting || (status is ConnectionStatus.Error && !stopped)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        observeConnectionStatus()
            .onEach { status -> _connectionStatus.value = status }
            .launchIn(viewModelScope)

        observeStockPrices()
            .onEach { stock -> _stocksBySymbol.update { it + (stock.symbol to stock) } }
            .launchIn(viewModelScope)
    }

    fun onFeedToggleClicked() {
        viewModelScope.launch {
            when (connectionStatus.value) {
                ConnectionStatus.Connected, ConnectionStatus.Connecting -> {
                    _manuallyStopped.value = true
                    disconnectStocksFeed()
                }

                else -> {
                    _manuallyStopped.value = false
                    connectStocksFeed()
                }
            }
        }
    }
}
