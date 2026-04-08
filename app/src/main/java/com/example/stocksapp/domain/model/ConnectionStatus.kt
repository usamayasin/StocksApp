package com.example.stocksapp.domain.model

sealed class ConnectionStatus {
    data object Disconnected : ConnectionStatus()
    data object Connecting : ConnectionStatus()
    data object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}
