package com.example.stocksapp.di

import com.example.stocksapp.data.mapper.StockDtoToDomainMapper
import com.example.stocksapp.data.mapper.StockDtoToDomainMapperImpl
import com.example.stocksapp.data.repository.StocksRepositoryImpl
import com.example.stocksapp.data.network.WebSocketDataSource
import com.example.stocksapp.data.network.WebSocketDataSourceImpl
import com.example.stocksapp.domain.repository.StocksRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    /**
     * Single process-wide scope for singletons (WebSocket, etc.).
     * Injected as plain [CoroutineScope] — no qualifier needed while this is the only binding.
     */
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindModule {

    @Binds
    @Singleton
    abstract fun bindWebSocketDataSource(impl: WebSocketDataSourceImpl): WebSocketDataSource

    @Binds
    @Singleton
    abstract fun bindStocksRepository(impl: StocksRepositoryImpl): StocksRepository

    @Binds
    @Singleton
    abstract fun bindStockDtoToDomainMapper(impl: StockDtoToDomainMapperImpl): StockDtoToDomainMapper
}
