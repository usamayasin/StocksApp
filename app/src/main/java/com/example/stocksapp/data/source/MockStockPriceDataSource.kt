package com.example.stocksapp.data.source

import com.example.stocksapp.data.model.StockDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockStockPriceDataSource @Inject constructor() {

    companion object {
        private const val UPDATE_INTERVAL_MS = 2_000L

        private const val MIN_DELTA_PERCENT = -0.05
        private const val MAX_DELTA_PERCENT = 0.05

        private val STOCK_SYMBOLS = listOf(
            "AAPL" to 175.50,
            "GOOG" to 142.30,
            "MSFT" to 378.85,
            "AMZN" to 151.94,
            "TSLA" to 248.50,
            "NVDA" to 485.20,
            "META" to 485.58,
            "NFLX" to 485.90,
            "AMD" to 144.25,
            "INTC" to 44.12,
            "ORCL" to 125.67,
            "CRM" to 278.45,
            "ADBE" to 567.89,
            "PYPL" to 62.34,
            "UBER" to 68.45,
            "LYFT" to 12.56,
            "SPOT" to 285.67,
            "TWTR" to 53.70,
            "SNAP" to 11.23,
            "PINS" to 33.45,
            "SQ" to 78.90,
            "SHOP" to 45.67,
            "ZM" to 65.43,
            "DOCU" to 54.32,
            "RBLX" to 42.10
        )

        val COMPANY_NAMES: Map<String, String> = mapOf(
            "AAPL" to "Apple Inc.",
            "GOOG" to "Alphabet Inc.",
            "MSFT" to "Microsoft Corp.",
            "AMZN" to "Amazon.com, Inc.",
            "TSLA" to "Tesla, Inc.",
            "NVDA" to "NVIDIA Corp.",
            "META" to "Meta Platforms",
            "NFLX" to "Netflix, Inc.",
            "AMD" to "Advanced Micro Devices",
            "INTC" to "Intel Corp.",
            "ORCL" to "Oracle Corp.",
            "CRM" to "Salesforce Inc.",
            "ADBE" to "Adobe Inc.",
            "PYPL" to "PayPal Holdings",
            "UBER" to "Uber Technologies",
            "LYFT" to "Lyft, Inc.",
            "SPOT" to "Spotify Technology",
            "TWTR" to "Twitter, Inc.",
            "SNAP" to "Snap Inc.",
            "PINS" to "Pinterest, Inc.",
            "SQ" to "Block, Inc.",
            "SHOP" to "Shopify Inc.",
            "ZM" to "Zoom Video Communications",
            "DOCU" to "DocuSign, Inc.",
            "RBLX" to "Roblox Corp."
        )

        private val COMPANY_DOMAINS: Map<String, String> = mapOf(
            "AAPL" to "apple.com",
            "GOOG" to "google.com",
            "MSFT" to "microsoft.com",
            "AMZN" to "amazon.com",
            "TSLA" to "tesla.com",
            "NVDA" to "nvidia.com",
            "META" to "meta.com",
            "NFLX" to "netflix.com",
            "AMD" to "amd.com",
            "INTC" to "intel.com",
            "ORCL" to "oracle.com",
            "CRM" to "salesforce.com",
            "ADBE" to "adobe.com",
            "PYPL" to "paypal.com",
            "UBER" to "uber.com",
            "LYFT" to "lyft.com",
            "SPOT" to "spotify.com",
            "TWTR" to "x.com",
            "SNAP" to "snapchat.com",
            "PINS" to "pinterest.com",
            "SQ" to "block.xyz",
            "SHOP" to "shopify.com",
            "ZM" to "zoom.us",
            "DOCU" to "docusign.com",
            "RBLX" to "roblox.com"
        )

        fun getCompanyName(symbol: String): String = COMPANY_NAMES[symbol] ?: symbol

        fun getCompanyLogoUrl(symbol: String): String? {
            val domain = COMPANY_DOMAINS[symbol] ?: return null
            return "https://www.google.com/s2/favicons?sz=64&domain=$domain"
        }
    }

    private val basePrices: Map<String, Double> = STOCK_SYMBOLS.toMap()

    private val currentPrices: MutableMap<String, Double> =
        STOCK_SYMBOLS.toMap().toMutableMap()

    /**
     * Emits all 25 symbols each tick: initial snapshot first ([onStart]), then updates every [UPDATE_INTERVAL_MS].
     */
    fun generatePriceUpdates(): Flow<List<StockDto>> = flow {
        while (true) {
            emit(generateUpdatesForAllSymbols())
            delay(UPDATE_INTERVAL_MS)
        }
    }.onStart {
        emit(generateInitialPrices())
    }

    private fun generateInitialPrices(): List<StockDto> {
        val ts = System.currentTimeMillis()
        return basePrices.map { (symbol, basePrice) ->
            StockDto(
                symbol = symbol,
                companyName = getCompanyName(symbol),
                logoUrl = getCompanyLogoUrl(symbol),
                price = basePrice,
                previousPrice = basePrice,
                timestamp = ts
            )
        }
    }

    private fun generateUpdatesForAllSymbols(): List<StockDto> {
        val timestamp = System.currentTimeMillis()
        return currentPrices.map { (symbol, currentPrice) ->
            val basePrice = basePrices[symbol] ?: currentPrice
            val previousPrice = currentPrice

            val deltaPercent = Random.nextDouble(MIN_DELTA_PERCENT, MAX_DELTA_PERCENT)
            val priceChange = basePrice * deltaPercent
            val newPrice = (currentPrice + priceChange)
                .coerceIn(basePrice * 0.5, basePrice * 1.5)

            currentPrices[symbol] = newPrice

            StockDto(
                symbol = symbol,
                companyName = getCompanyName(symbol),
                logoUrl = getCompanyLogoUrl(symbol),
                price = newPrice,
                previousPrice = previousPrice,
                timestamp = timestamp
            )
        }
    }

    fun getCurrentPrice(symbol: String): Double? = currentPrices[symbol]

    fun getAllCurrentPrices(): Map<String, Double> = currentPrices.toMap()
}
