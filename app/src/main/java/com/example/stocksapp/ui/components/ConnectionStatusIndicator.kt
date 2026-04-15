package com.example.stocksapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stocksapp.R
import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.ui.theme.StockUpGreen
import com.example.stocksapp.ui.theme.StocksAppTheme

@Composable
fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    val color = when (status) {
        is ConnectionStatus.Connected -> StockUpGreen
        is ConnectionStatus.Connecting -> Color(0xFFFFA000)
        is ConnectionStatus.Disconnected -> Color(0xFFC62828)
        is ConnectionStatus.Error -> Color(0xFFC62828)
    }
    val desc = stringResource(
        when (status) {
            is ConnectionStatus.Connected -> R.string.cd_status_connected
            is ConnectionStatus.Connecting -> R.string.cd_status_connecting
            is ConnectionStatus.Disconnected -> R.string.cd_status_disconnected
            is ConnectionStatus.Error -> R.string.cd_status_error
        },
    )
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Preview(showBackground = true, name = "ConnectionStatusIndicator")
@Composable
private fun ConnectionStatusIndicatorPreview() {
    StocksAppTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConnectionStatusIndicator(ConnectionStatus.Connected)
            ConnectionStatusIndicator(ConnectionStatus.Connecting)
            ConnectionStatusIndicator(ConnectionStatus.Disconnected)
            ConnectionStatusIndicator(ConnectionStatus.Error("Preview"))
        }
    }
}
