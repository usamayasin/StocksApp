package com.example.stocksapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.stocksapp.ui.navigation.StockDetailDestination
import com.example.stocksapp.ui.navigation.StocksListDestination
import com.example.stocksapp.ui.stocks.StockDetailScreen
import com.example.stocksapp.ui.stocks.StocksListScreen
import com.example.stocksapp.ui.stocks.StocksViewModel
import com.example.stocksapp.ui.theme.StocksAppTheme

@Composable
fun StocksApp(
    modifier: Modifier = Modifier,
) {
    val systemDark = isSystemInDarkTheme()
    var themeOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val useDarkTheme = themeOverride ?: systemDark

    StocksAppTheme(darkTheme = useDarkTheme) {
        val navController = rememberNavController()
        val viewModel: StocksViewModel = hiltViewModel()
        NavHost(
            navController = navController,
            startDestination = StocksListDestination,
            modifier = modifier,
        ) {
            composable<StocksListDestination> {
                StocksListScreen(
                    viewModel = viewModel,
                    onStockClick = { stock ->
                        navController.navigate(StockDetailDestination(stock.symbol))
                    },
                    darkTheme = useDarkTheme,
                    onDarkThemeToggle = {
                        themeOverride = !useDarkTheme
                    },
                )
            }
            composable<StockDetailDestination> { entry ->
                val destination = entry.toRoute<StockDetailDestination>()
                StockDetailScreen(
                    symbol = destination.symbol,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
