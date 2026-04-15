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
import androidx.navigation.navDeepLink
import com.example.stocksapp.ui.navigation.StockDetailDestination
import com.example.stocksapp.ui.navigation.StocksListDestination
import com.example.stocksapp.ui.detail.StockDetailScreen
import com.example.stocksapp.ui.detail.StockDetailViewModel
import com.example.stocksapp.ui.stocks.StocksListScreen
import com.example.stocksapp.ui.stocks.StocksListViewModel
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

        NavHost(
            navController = navController,
            startDestination = StocksListDestination,
            modifier = modifier,
        ) {
            composable<StocksListDestination> {

                val stockslistViewModel: StocksListViewModel = hiltViewModel()
                StocksListScreen(
                    viewModel = stockslistViewModel,
                    onStockClick = { stock ->
                        navController.navigate(StockDetailDestination(stock.symbol))
                    },
                    darkTheme = useDarkTheme,
                    onDarkThemeToggle = {
                        themeOverride = !useDarkTheme
                    },
                )
            }
            composable<StockDetailDestination>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "stocks://symbol/{symbol}"
                    },
                ),
            ) {
                val stockDetailViewModel: StockDetailViewModel = hiltViewModel()
                StockDetailScreen(
                    viewModel = stockDetailViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
