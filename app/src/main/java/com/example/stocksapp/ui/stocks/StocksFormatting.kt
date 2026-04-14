package com.example.stocksapp.ui.stocks

import com.example.stocksapp.domain.model.PriceDirection
import com.example.stocksapp.domain.model.Stock
import java.util.Locale
import kotlin.math.abs

internal fun Stock.changePercent(): Double {
    val previous = price - change
    if (abs(previous) < 1e-9) return 0.0
    return (change / previous) * 100.0
}

internal fun formatUsd(price: Double): String =
    String.format(Locale.US, "$%.2f", price)

internal fun formatChange(change: Double): String =
    String.format(Locale.US, "%+.2f", change)

internal fun formatChangePercent(percent: Double): String =
    String.format(Locale.US, "(%.2f%%)", percent)

/** Same visible line as list rows: ▲ +0.13 (3.23%) */
internal fun formatStockMovementLine(stock: Stock): String {
    val pct = stock.changePercent()
    return "${directionSymbol(stock.direction)} ${formatChange(stock.change)} ${formatChangePercent(pct)}"
}

internal fun formatSignedPercent(percent: Double): String =
    String.format(Locale.US, "%+.2f%%", percent)

internal fun directionSymbol(direction: PriceDirection): String =
    when (direction) {
        PriceDirection.UP -> "▲"
        PriceDirection.DOWN -> "▼"
        PriceDirection.UNCHANGED -> "—"
    }
