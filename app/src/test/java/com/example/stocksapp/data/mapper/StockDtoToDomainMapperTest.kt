package com.example.stocksapp.data.mapper

import com.example.stocksapp.data.model.StockDto
import org.junit.Assert.assertEquals
import org.junit.Test

class StockDtoToDomainMapperTest {

    private val mapper = StockDtoToDomainMapperImpl()

    @Test
    fun `toDomain maps dto fields and computes change`() {
        val dto = StockDto(
            symbol = "MSFT",
            companyName = "Microsoft",
            logoUrl = null,
            price = 410.50,
            previousPrice = 408.20,
            timestamp = 123L,
        )

        val domain = mapper.toDomain(dto)

        assertEquals("MSFT", domain.symbol)
        assertEquals("Microsoft", domain.companyName)
        assertEquals(null, domain.logoUrl)
        assertEquals(410.50, domain.price, 0.000001)
        assertEquals(2.30, domain.change, 0.000001)
    }
}
