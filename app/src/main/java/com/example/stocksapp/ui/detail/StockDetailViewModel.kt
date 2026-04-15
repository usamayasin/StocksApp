package com.example.stocksapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.usecase.ObserveStockPricesUseCase
import com.example.stocksapp.ui.navigation.StockDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeStockPrices: ObserveStockPricesUseCase,
) : ViewModel() {

    val symbol: String = savedStateHandle.readSymbol()

    private val _stock = MutableStateFlow<Stock?>(null)
    val stock: StateFlow<Stock?> = _stock.asStateFlow()

    init {
        observeStockPrices()
            .onEach { stock ->
                if (stock.symbol == symbol) {
                    _stock.value = stock
                }
            }
            .launchIn(viewModelScope)
    }
}

private fun SavedStateHandle.readSymbol(): String {
    return get<String>("symbol")
        ?: toRoute<StockDetailDestination>().symbol
}
