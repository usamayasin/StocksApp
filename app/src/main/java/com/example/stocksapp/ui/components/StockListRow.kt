package com.example.stocksapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stocksapp.domain.model.PriceDirection
import com.example.stocksapp.domain.model.Stock
import com.example.stocksapp.ui.stocks.formatStockMovementLine
import com.example.stocksapp.ui.stocks.formatUsd
import com.example.stocksapp.ui.theme.StockDownRed
import com.example.stocksapp.ui.theme.StockUpGreen
import com.example.stocksapp.ui.theme.StocksAppTheme

@Composable
fun StockListRow(
    stock: Stock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = when (stock.direction) {
        PriceDirection.UP -> StockUpGreen
        PriceDirection.DOWN -> StockDownRed
        PriceDirection.UNCHANGED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StockLogoImage(
            symbol = stock.symbol,
            logoUrl = stock.logoUrl,
            size = 24.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stock.companyName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatUsd(stock.price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
            Text(
                text = formatStockMovementLine(stock),
                style = MaterialTheme.typography.bodySmall,
                color = accent,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Preview(showBackground = true, name = "StockListRow")
@Composable
private fun StockListRowPreview() {
    StocksAppTheme {
        StockListRow(
            stock = Stock(
                symbol = "AAPL",
                companyName = "Apple Inc.",
                logoUrl = null,
                price = 175.50,
                change = 2.25,
            ),
            onClick = {},
        )
    }
}
