package com.example.stocksapp.data.mapper

import com.example.stocksapp.data.model.ConnectionState
import com.example.stocksapp.domain.model.ConnectionStatus

fun ConnectionState.toDomain(): ConnectionStatus = when (this) {
    ConnectionState.Disconnected -> ConnectionStatus.Disconnected
    ConnectionState.Connecting -> ConnectionStatus.Connecting
    ConnectionState.Connected -> ConnectionStatus.Connected
    is ConnectionState.Error -> ConnectionStatus.Error(message)
}
