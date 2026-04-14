package com.example.stocksapp.ui.stocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stocksapp.R
import com.example.stocksapp.domain.model.ConnectionStatus
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.domain.model.isInConnectedState
import com.example.stocksapp.ui.components.ConnectionStatusIndicator
import com.example.stocksapp.ui.components.StockListRow
import com.example.stocksapp.ui.components.StocksEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StocksListScreen(
    viewModel: StocksViewModel,
    onStockClick: (Stock) -> Unit,
    darkTheme: Boolean,
    onDarkThemeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stocks = viewModel.stocks.collectAsStateWithLifecycle()
    val connectionStatus = viewModel.connectionStatus.collectAsStateWithLifecycle()
    val showConnectionLoader = viewModel.showConnectionLoader.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val genericErrorMessage = stringResource(R.string.msg_connection_error_generic)

    LaunchedEffect(connectionStatus.value) {
        (connectionStatus.value as? ConnectionStatus.Error)?.let { error ->
            val message = error.message.ifBlank { genericErrorMessage }
            snackbarHostState.showSnackbar(message = message)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                        modifier = Modifier.padding(start = 10.dp),
                    ) {
                        ConnectionStatusIndicator(status = connectionStatus.value)
                        Text(
                            text = stringResource(R.string.stock_price_pulse_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                actions = {
                    val connected = connectionStatus.value.isInConnectedState()
                    val desc = if (connected) {
                        stringResource(R.string.cd_pause_feed)
                    } else {
                        stringResource(R.string.cd_start_feed)
                    }
                    val toggleThemeDesc = stringResource(R.string.cd_toggle_theme)
                    if (showConnectionLoader.value) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.onFeedToggleClicked() },
                            modifier = Modifier.semantics { contentDescription = desc },
                        ) {
                            Icon(
                                imageVector = if (connected) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null,
                            )
                        }
                    }
                    IconButton(
                        onClick = onDarkThemeToggle,
                        modifier = Modifier.semantics {
                            contentDescription = toggleThemeDesc
                        },
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Filled.WbSunny else Icons.Filled.NightlightRound,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                stocks.value.isEmpty() &&
                    connectionStatus.value.isInConnectedState() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                stocks.value.isEmpty() -> StocksEmptyState(status = connectionStatus.value)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(stocks.value, key = { it.symbol }) { stock ->
                        Column {
                            StockListRow(
                                stock = stock,
                                onClick = { onStockClick(stock) },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}
