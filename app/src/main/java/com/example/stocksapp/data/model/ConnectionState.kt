package com.example.stocksapp.data.model

/**
 * Transport-layer connection state for WebSocket / networking.
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
