package com.example.stocksapp.domain.model

sealed class ConnectionStatus {
    data object Disconnected : ConnectionStatus()
    data object Connecting : ConnectionStatus()
    data object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

fun ConnectionStatus.isDisconnected(): Boolean = this is ConnectionStatus.Disconnected

fun ConnectionStatus.isConnecting(): Boolean = this is ConnectionStatus.Connecting

fun ConnectionStatus.isInConnectedState(): Boolean = this is ConnectionStatus.Connected

fun ConnectionStatus.isError(): Boolean = this is ConnectionStatus.Error

fun ConnectionStatus.errorMessageOrNull(): String? =
    (this as? ConnectionStatus.Error)?.message
