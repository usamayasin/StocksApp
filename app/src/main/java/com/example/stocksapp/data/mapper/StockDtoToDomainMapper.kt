package com.example.stocksapp.data.mapper

import com.example.stocksapp.data.model.StockDto
import com.example.stocksapp.domain.model.Stock
import javax.inject.Inject
import javax.inject.Singleton

fun interface StockDtoToDomainMapper {
    fun toDomain(dto: StockDto): Stock
}

@Singleton
class StockDtoToDomainMapperImpl @Inject constructor() : StockDtoToDomainMapper {

    override fun toDomain(dto: StockDto): Stock = Stock(
        symbol = dto.symbol,
        companyName = dto.companyName,
        logoUrl = dto.logoUrl,
        price = dto.price,
        change = dto.price - dto.previousPrice
    )
}
