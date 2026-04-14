package com.example.stocksapp.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.stocksapp.ui.theme.StocksAppTheme

@Composable
fun StockLogoImage(
    symbol: String,
    logoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(logoUrl)
            .crossfade(true)
            .build(),
        contentDescription = "$symbol logo",
        modifier = modifier
            .size(size)
            .clip(CircleShape),
    )
}

@Preview(showBackground = true, name = "StockLogoImage")
@Composable
private fun StockLogoImagePreview() {
    StocksAppTheme {
        StockLogoImage(symbol = "AAPL", logoUrl = null, size = 32.dp)
    }
}
