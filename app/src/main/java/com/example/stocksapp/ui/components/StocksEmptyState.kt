package com.example.stocksapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stocksapp.R
import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.ui.theme.StocksAppTheme

@Composable
fun StocksEmptyState(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    val message = when (status) {
        is ConnectionStatus.Error -> status.message.ifBlank {
            stringResource(R.string.msg_connection_error_generic)
        }
        is ConnectionStatus.Connecting -> stringResource(R.string.msg_connecting)
        else -> stringResource(R.string.msg_press_start)
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, name = "Empty — press start")
@Composable
private fun StocksEmptyStateDisconnectedPreview() {
    StocksAppTheme {
        StocksEmptyState(ConnectionStatus.Disconnected)
    }
}

@Preview(showBackground = true, name = "Empty — error")
@Composable
private fun StocksEmptyStateErrorPreview() {
    StocksAppTheme {
        StocksEmptyState(ConnectionStatus.Error("No network"))
    }
}
